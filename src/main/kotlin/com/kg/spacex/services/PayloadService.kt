package com.kg.spacex.services

import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.PayloadRepository
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import com.kg.spacex.utils.SPACEX_API_PAYLOADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PayloadService(
    private val spaceXAPIService: SpaceXAPIService,
    private var repo: PayloadRepository,
    private var launchRepo: LaunchRepository
) {

    fun fetchOne(
        payloadId: String
    ): PayloadInternal {
        val result = spaceXAPIService.handleAPICall(
            url = SPACEX_API_PAYLOADS_URL.plus(payloadId),
            deserializer = PayloadInternal.Deserializer()
        ) as PayloadInternal?
        return result ?: throw ResourceUnavailableException()
    }

    fun fetchAll(): List<PayloadInternal> {
        val payloads = mutableListOf<PayloadInternal>()
        val resultList = spaceXAPIService.handleAPICall(
            url = SPACEX_API_PAYLOADS_URL,
            deserializer = PayloadInternal.ArrayDeserializer()
        ) as Array<*>? ?: throw ResourceUnavailableException()
        resultList.forEach { payloads.add(it as PayloadInternal) }
        return payloads
    }

    /**
     * Will save a payload only if its associated launch's data has been saved from launches API endpoint,
     * since not all launch data is available via API.
     */
    fun saveOrUpdate(
        payloads: List<PayloadInternal>
    ) {
        payloads.forEach { payload ->
            val payloadExternal = convertToExternal(payload)
            val foundLaunch = launchRepo.findByIdOrNull(payloadExternal.launchId)
            if (foundLaunch != null) repo.save(payloadExternal)
        }
    }

    /**
     * Converts PayloadInternal to PayloadExternal by transforming the attributes customers, nationalities, and
     * manufacturers from List<String> to comma-separated Strings, in order to simplify database architecture.
     * The values in these attributes are never accessed otherwise, so lost flexibility is not a concern.
     */
    fun convertToExternal(
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

    fun getById(
        payloadId: String
    ): PayloadExternal {
        return repo.findByIdOrNull(payloadId) ?: throw ResourceNotFoundException()
    }

    fun getAllByLaunchId(
        launchId: String
    ): List<PayloadExternal> {
        val payloads = mutableListOf<PayloadExternal>()
        val foundPayloads = repo.findAllByLaunchId(launchId)
        return if (foundPayloads.isEmpty()) {
            emptyList()
        } else {
            foundPayloads.forEach { foundPayload -> payloads.add(foundPayload) }
            payloads
        }
    }
}
