package com.kg.spacex.services

import com.kg.spacex.models.Capsule
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import org.springframework.stereotype.Service

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    /**
     * Fetches a list of Capsules that match the provided id(s).
     *
     * @param capsuleIds is a list of ids for capsules.
     * @return a list of Capsules or null, if the API call fails or returns null.
     */
    fun fetchCapsules(
        capsuleIds: List<String>
    ): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        capsuleIds.forEach { id ->
            val apiResult = spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL.plus(id),
                deserializer = Capsule.Deserializer()
            ) ?: return null
            capsules.add(apiResult as Capsule)
        }
        return capsules
    }
}
