package com.kg.spacex.services

import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.utils.SPACEX_API_PAYLOADS_URL
import org.springframework.stereotype.Service
import java.util.logging.Logger

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
    ): List<PayloadInternal>? {
        val fetchedPayloads = mutableListOf<PayloadInternal>()
        payloadIds.forEach { id ->
            val apiResult = spaceXAPIService.handleAPICall(
                url = SPACEX_API_PAYLOADS_URL.plus(id),
                deserializer = PayloadInternal.Deserializer()
            ) ?: return null
            fetchedPayloads.add(apiResult as PayloadInternal)
        }
        return fetchedPayloads
    }

    fun convertToPayloadStored(
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

    // TODO add func to save PayloadExternals
    //  must convert custs, nations, manufs from List<String> to <String> using
    //  .joinToString()
}
