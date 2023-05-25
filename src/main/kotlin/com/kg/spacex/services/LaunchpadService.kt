package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.springframework.stereotype.Service

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class LaunchpadService (private val spaceXAPIService: SpaceXAPIService) {

    /**
     * Fetches a Launchpad that matches the provided id.
     *
     * @param launchpadId is the id for a launchpad.
     * @return Launchpad or null, if the API call fails or returns null.
     */
    fun fetchLaunchpad(
        launchpadId: String?
    ): Launchpad? {
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHPADS_URL.plus(launchpadId),
            deserializer = Launchpad.Deserializer()
        ) ?: return null
        return apiResult as Launchpad
    }
}
