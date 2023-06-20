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
    private val spaceXAPIService: SpaceXAPIService,

) {
    @Autowired
    private lateinit var db: PayloadRepository

    @Autowired
    private lateinit var launchRepo: LaunchRepository

    val logger = Logger.getLogger("logger")

    fun fetchOnePayload(
        payloadId: String
    ) {
        val apiResult = makeAPICall(payloadId) as PayloadInternal
        val newPayload = prepareToSavePayload(apiResult)
        updateOrSavePayload(newPayload)
    }

    fun fetchAndSaveAllPayloads() {
        val apiResult = makeAPICall(null) as Array<*>
        apiResult.forEach { result ->
            result as PayloadInternal
            val newPayload = prepareToSavePayload(result as PayloadInternal)
            updateOrSavePayload(newPayload)
        }
    }

    fun makeAPICall(
        payloadId: String?
    ): Any {
        return if (payloadId != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL.plus(payloadId),
                deserializer = PayloadInternal.Deserializer()
            ) as PayloadInternal
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL,
                deserializer = PayloadInternal.ArrayDeserializer()
            ) as Array<*>
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

    fun updateOrSavePayload(
        payload: PayloadExternal
    ) {
        val foundPayload = db.findByIdOrNull(payload.id)
        if (foundPayload != null) {
            foundPayload.name = payload.name
            foundPayload.type = payload.type
            foundPayload.regime = payload.regime
            foundPayload.customers = payload.customers
            foundPayload.nationalities = payload.nationalities
            foundPayload.manufacturers = payload.manufacturers
            foundPayload.mass_kg = payload.mass_kg
            foundPayload.mass_lbs = payload.mass_lbs
            db.save(foundPayload)
        } else {
            // only save payload if its launch data is offered through launches API endpoint
            val foundLaunch = launchRepo.findByIdOrNull(payload.launchId)
            if (foundLaunch == null) {
                logger.info("Launch ${payload.launchId} for payload ${payload.id} not found in Launches")
            } else {
                db.save(payload)
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
        val foundPayloads = db.findAllByLaunchId(launchId) ?: return null
        foundPayloads.forEach { payloads.add(it) }
        return payloads
    }
}
