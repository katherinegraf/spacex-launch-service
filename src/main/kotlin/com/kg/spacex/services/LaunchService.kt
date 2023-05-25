package com.kg.spacex.services

import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.utils.SPACEX_API_LAUNCHES_URL
import org.springframework.stereotype.Service

/**
 * Connects to [capsuleService], [launchpadService], [payloadService], and [spaceXAPIService]
 * to access the calling methods in those service classes.
 */
@Service
class LaunchService (
    private val capsuleService: CapsuleService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService,
    private val spaceXAPIService: SpaceXAPIService
) {

    /**
     * Gets a single LaunchExternal object from SpaceX API using the provided id.
     *
     * First calls to fetch the LaunchInternal object for the provided id,
     * then calls to fetch the payload, launchpad, capsule details to transform it
     * into a LaunchExternal object. Returns null if either fetch call returns null.
     *
     * @param launchId is the id of a launch object.
     * @return one LaunchExternal object or null.
     */
    fun getOneLaunchExternal(
        launchId: String
    ): LaunchExternal? {
        val launchInternal = fetchOneLaunchFromSpaceX(launchId) ?: return null
        return fetchLaunchExternalForLaunchInternal(launchInternal)
    }

    /**
     * Gets all launches with expanded payload/launchpad/capsule details from SpaceX API.
     *
     * Calls [fetchAllLaunchesFromSpaceX] to get all possible LaunchInternals,
     * iterates over the returned List<LaunchInternal>, passing each element through
     * [fetchLaunchExternalForLaunchInternal] to transform them to LaunchExternals,
     * then builds & returns a List<LaunchExternal>.
     * Returns null if either fetch call returns null.
     *
     * @return list of all LaunchExternals or null.
     */
    fun getListOfLaunchExternals(): List<LaunchExternal>? {
        val launchExternals = mutableListOf<LaunchExternal>()
        val launchInternals = fetchAllLaunchesFromSpaceX() ?: return null
        launchInternals.forEach { launch ->
            val launchExternal = fetchLaunchExternalForLaunchInternal(launch) ?: return null
            launchExternals.add(launchExternal)
        }
        return launchExternals
    }

    /**
     * Gets a single LaunchInternal object from SpaceX API using the provided id.
     *
     * Calls the API for the LaunchInternal object for the provided id.
     * Returns null if fetch call returns null.
     *
     * @param launchId is the id of a launch object.
     * @return one LaunchInternal object or null.
     */
    fun fetchOneLaunchFromSpaceX(
        launchId: String
    ): LaunchInternal? {
        return spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL.plus(launchId),
            deserializer = LaunchInternal.Deserializer()
        ) as LaunchInternal?
    }

    /**
     * Gets all launches with payload/launchpad/capsule ids from SpaceX API.
     *
     * Fetches all launches from SpaceX API as a nullable generically-typed array, then
     * iterates over array and casts each element as LaunchInternal before adding it
     * to a list. Returns list of LaunchInternals, or null if the API call fails or returns null.
     *
     * @return list of LaunchInternals or null.
     */
    fun fetchAllLaunchesFromSpaceX(): List<LaunchInternal>? {
        val launchInternals = mutableListOf<LaunchInternal>()
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL,
            deserializer = LaunchInternal.ArrayDeserializer()
        ) as Array<*>? ?: return null
        apiResult.forEach { launch ->
            launchInternals.add(launch as LaunchInternal)
        }
        return launchInternals
    }

    /**
     * Transforms LaunchInternal into LaunchExternal.
     *
     * Fetches details for payloads, launchpad, and capsules based on ids found in provided
     * LaunchInternal. Uses these additional details to transform LaunchInternal into LaunchExternal.
     * Returns null if any of the fetches fail or return null, else returns a LaunchExternal object.
     *
     * @param launch is a LaunchInternal object.
     * @return a LaunchExternal object or null.
     */
    fun fetchLaunchExternalForLaunchInternal(
        launch: LaunchInternal
    ): LaunchExternal? {
        val launchpad = launchpadService.fetchLaunchpad(launch.launchpad) ?: return null
        val payloads = payloadService.fetchPayloads(launch.payloads) ?: return null
        val capsules = capsuleService.fetchCapsules(launch.capsules) ?: return null
        return LaunchExternal(
                launch.name,
                launch.details,
                launch.date_utc,
                launch.success,
                launch.failures,
                launch.id,
                launchpad,
                payloads,
                capsules
            )
    }
}
