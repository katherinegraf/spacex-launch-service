package com.kg.spacex.services

import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.PayloadRepository
import com.kg.spacex.utils.SPACEX_API_PAYLOADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class PayloadService(
    private val spaceXAPIService: SpaceXAPIService
) {
    @Autowired
    private lateinit var db: PayloadRepository

    @Autowired
    private lateinit var launchRepo: LaunchRepository

    val logger = Logger.getLogger("logger")

    fun fetchOnePayload(
        payloadId: String
    ): Boolean {
        val result = makeAPICall(payloadId) as PayloadInternal?
        return result !=null
    }

    fun fetchAllPayloads(): Boolean {
        val resultList = makeAPICall(null) as Array<*>?
        return !(resultList == null || resultList.isEmpty())
    }

    fun makeAPICall(
        payloadId: String?
    ): Any? {
        return if (payloadId != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL.plus(payloadId),
                deserializer = PayloadInternal.Deserializer()
            )
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL,
                deserializer = PayloadInternal.ArrayDeserializer()
            )
        }
    }

    fun prepareToSavePayload(
        payload: PayloadInternal
    ): PayloadExternal {
        return PayloadExternal(
            id = payload.id,
            name = payload.name,
            type = payload.type,
            regime = payload.regime,
            launchId = payload.launchId,
            customers = payload.customers.joinToString(),
            nationalities = payload.nationalities.joinToString(),
            manufacturers = payload.manufacturers.joinToString(),
            mass_kg = payload.mass_kg,
            mass_lbs = payload.mass_lbs
        )
    }

    fun updateOrSavePayloads(
        payloads: List<PayloadInternal>
    ) {
        payloads.forEach { payload ->
            val payloadExternal = prepareToSavePayload(payload)
            val foundPayload = db.findByIdOrNull(payload.id)
            if (foundPayload != null) {
                logger.info("Didn't find payload")
                foundPayload.name = payloadExternal.name
                foundPayload.type = payloadExternal.type
                foundPayload.regime = payloadExternal.regime
                foundPayload.customers = payloadExternal.customers
                foundPayload.nationalities = payloadExternal.nationalities
                foundPayload.manufacturers = payloadExternal.manufacturers
                foundPayload.mass_kg = payloadExternal.mass_kg
                foundPayload.mass_lbs = payloadExternal.mass_lbs
                db.save(foundPayload)
            } else {
                // only save payload if its launch data is offered through launches API endpoint
                val foundLaunch = launchRepo.findByIdOrNull(payloadExternal.launchId)
                if (foundLaunch == null) {
                    logger.info("Launch ${payloadExternal.launchId} for payload ${payloadExternal.id} not found in Launches")
                } else {
                    db.save(payloadExternal)
                }
            }
        }
    }

    fun getPayloadByID(
        payloadId: String
    ): PayloadExternal? {
        return db.findByIdOrNull(payloadId)
    }

    fun getPayloadsByLaunchId(
        launchId: String
    ): List<PayloadExternal>? {
        val payloads = mutableListOf<PayloadExternal>()
        val foundPayloads = db.findAllByLaunchId(launchId)
        return if (foundPayloads.isEmpty()) {
            logger.warning("Match not found in payload repo")
            null
        } else {
            foundPayloads.forEach { foundPayload -> payloads.add(foundPayload) }
            return payloads
        }
    }
}
