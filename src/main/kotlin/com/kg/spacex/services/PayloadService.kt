package com.kg.spacex.services

import com.kg.spacex.models.Payload
import com.kg.spacex.utils.SPACEX_API_PAYLOADS_URL
import org.springframework.stereotype.Service

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class PayloadService(private val spaceXAPIService: SpaceXAPIService) {

    /**
     * Fetches a list of Payloads that match the provided id(s).
     *
     * @param payloadIds is a list of ids for payloads.
     * @return a list of Payloads or null, if the API call fails or returns null.
     */
    fun fetchPayloads(
        payloadIds: List<String>
    ): List<Payload>? {
        val payloads = mutableListOf<Payload>()
        payloadIds.forEach { id ->
            val apiResult = spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL.plus(id),
                deserializer = Payload.Deserializer()
            ) ?: return null
            payloads.add(apiResult as Payload)
        }
        return payloads
    }
}
