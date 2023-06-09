package com.kg.spacex.services

import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.LaunchpadRepository
import com.kg.spacex.utils.SPACEX_API_LAUNCHES_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Connects to [capsuleService], [launchpadService], [payloadService], and [spaceXAPIService]
 * to access the calling methods in those service classes.
 */
@Service
class LaunchService (
    private val capsuleService: CapsuleService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService,
    private val spaceXAPIService: SpaceXAPIService,
    private val failureService: FailureService,
) {

    @Autowired
    private lateinit var db: LaunchRepository

    @Autowired
    private lateinit var launchpadRepo: LaunchpadRepository

    @Autowired
    private lateinit var capsuleRepo: CapsuleRepository

    val logger = Logger.getLogger("logger")

//    /**
//     * Gets a single LaunchExternal object from SpaceX API using the provided id.
//     *
//     * First calls to fetch the LaunchInternal object for the provided id,
//     * then calls to fetch the payload, launchpad, capsule details to transform it
//     * into a LaunchExternal object. Returns null if either fetch call returns null.
//     *
//     * @param launchId is the id of a launch object.
//     * @return one LaunchExternal object or null.
//     */
//    fun getOneLaunchExternal(
//        launchId: String
//    ): LaunchExternal? {
//        val launchInternal = fetchOneLaunchFromSpaceX(launchId) ?: return null
//        return fetchLaunchExternalForLaunchInternal(launchInternal)
//    }
//
//    /**
//     * Gets all launches with expanded payload/launchpad/capsule details from SpaceX API.
//     *
//     * Calls [fetchAllLaunchesFromSpaceX] to get all possible LaunchInternals,
//     * iterates over the returned List<LaunchInternal>, passing each element through
//     * [fetchLaunchExternalForLaunchInternal] to transform them to LaunchExternals,
//     * then builds & returns a List<LaunchExternal>.
//     * Returns null if either fetch call returns null.
//     *
//     * @return list of all LaunchExternals or null.
//     */
//    fun getListOfLaunchExternals(): List<LaunchExternal>? {
//        val launchExternals = mutableListOf<LaunchExternal>()
//        val launchInternals = fetchAllLaunchesFromSpaceX() ?: return null
//        launchInternals.forEach { launch ->
//            val launchExternal = fetchLaunchExternalForLaunchInternal(launch) ?: return null
//            launchExternals.add(launchExternal)
//        }
//        return launchExternals
//    }
//
//    /**
//     * Gets a single LaunchInternal object from SpaceX API using the provided id.
//     *
//     * Calls the API for the LaunchInternal object for the provided id.
//     * Returns null if fetch call returns null.
//     *
//     * @param launchId is the id of a launch object.
//     * @return one LaunchInternal object or null.
//     */
//    fun fetchOneLaunchFromSpaceX(
//        launchId: String
//    ): LaunchInternal? {
//        return spaceXAPIService.handleAPICall(
//            url = SPACEX_API_LAUNCHES_URL.plus(launchId),
//            deserializer = LaunchInternal.Deserializer()
//        ) as LaunchInternal?
//    }
//
//    /**
//     * Gets all launches with payload/launchpad/capsule ids from SpaceX API.
//     *
//     * Fetches all launches from SpaceX API as a nullable generically-typed array, then
//     * iterates over array and casts each element as LaunchInternal before adding it
//     * to a list. Returns list of LaunchInternals, or null if the API call fails or returns null.
//     *
//     * @return list of LaunchInternals or null.
//     */
//    fun fetchAllLaunchesFromSpaceX(): List<LaunchInternal>? {
//        val launchInternals = mutableListOf<LaunchInternal>()
//        val apiResult = spaceXAPIService.handleAPICall(
//            url = SPACEX_API_LAUNCHES_URL,
//            deserializer = LaunchInternal.ArrayDeserializer()
//        ) as Array<*>? ?: return null
//        apiResult.forEach { launch ->
//            launchInternals.add(launch as LaunchInternal)
//        }
//        return launchInternals
//    }
//
//    /**
//     * Transforms LaunchInternal into LaunchExternal.
//     *
//     * Fetches details for payloads, launchpad, and capsules based on ids found in provided
//     * LaunchInternal. Uses these additional details to transform LaunchInternal into LaunchExternal.
//     * Returns null if any of the fetches fail or return null, else returns a LaunchExternal object.
//     *
//     * @param launch is a LaunchInternal object.
//     * @return a LaunchExternal object or null.
//     */
//    fun fetchLaunchExternalForLaunchInternal(
//        launch: LaunchInternal
//    ): LaunchExternal? {
//        val failures = failureService.convertFailureInternalToFailureExternal(launch.failures, launch.id)
//        val launchpad = launchpadService.fetchLaunchpad(launch.launchpadId) ?: return null
//        val fetchedPayloads = payloadService.fetchPayloads(launch.payloadIds) ?: return null
//        val payloadsForLaunchExternal = mutableListOf<PayloadExternal>()
//        fetchedPayloads.forEach {
//            val p = payloadService.convertToPayloadExternal(it)
//            payloadsForLaunchExternal.add(p)
//        }
//        val capsules = capsuleService.fetchCapsules(launch.capsuleIds) ?: return null
//        return LaunchExternal(
//                launch.name,
//                launch.details,
//                launch.date_utc,
//                launch.success,
//                failures,
//                launch.id,
//                launchpad,
////                payloadsForLaunchExternal,
//                capsules
//            )
//    }

    fun fetchAndSaveOneLaunch(
        launchId: String
    ): LaunchExternal? {
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL.plus(launchId),
            deserializer = LaunchInternal.Deserializer()
        ) ?: return null
        val launchInternal = apiResult as LaunchInternal
        val failureExternals = failureService.externalizeFailure(
                launchInternal.failures,
                launchInternal.id
            )
        failureService.updateOrSaveFailures(failureExternals)
        val newLaunchExternal = externalizeLaunch(
            launchInternal,
            failureExternals
        ) ?: return null
        updateOrSaveLaunch(newLaunchExternal)
        return newLaunchExternal
    }

    fun fetchAndSaveAllLaunches(): List<LaunchExternal>? {
        /*
        gets list of launches from spacex
        for each:
            converts failures
            converts launch
            saves launch
            adds launch to return list
            saves failure
         */
        val launches = mutableListOf<LaunchExternal>()
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL,
            deserializer = LaunchInternal.ArrayDeserializer()
        ) as Array<*>? ?: return null
        apiResult.forEach { launchInternal ->
            launchInternal as LaunchInternal
            val failureExternals = failureService.externalizeFailure(
                launchInternal.failures,
                launchInternal.id
            )
            val newLaunchExternal = externalizeLaunch(
                launchInternal,
                failureExternals
            ) ?: return null
            updateOrSaveLaunch(newLaunchExternal)
            failureService.updateOrSaveFailures(failureExternals)
            launches.add(newLaunchExternal)
        }
        return launches
    }

    fun externalizeLaunch(
        launchInternal: LaunchInternal,
        failures: List<FailureExternal>
    ): LaunchExternal? {
//        val launchpad = launchpadRepo.findByIdOrNull(launchInternal.launchpadId) ?: return null
        val launchpad = launchpadService.findLaunchpadOrNull(launchInternal.launchpadId) ?: run {
            logger.warning("Failed to retrieve launchpad ${launchInternal.launchpadId}")
            return null
        }
        val capsules = capsuleService.findCapsulesById(launchInternal.capsuleIds) ?: run {
            logger.warning("Failed to retrieve capsules ${launchInternal.capsuleIds}")
            return null
        }
        val newLaunchExternal = LaunchExternal(
            name = launchInternal.name,
            details = launchInternal.details,
            date_utc = launchInternal.date_utc,
            success = launchInternal.success,
            failures = failures,
            id = launchInternal.id,
            launchpad = launchpad,
            capsules = capsules
        )
        updateOrSaveLaunch(newLaunchExternal)
        return newLaunchExternal
    }

    fun updateOrSaveLaunch(
        launch: LaunchExternal
    ) {
        val foundLaunch = db.findByIdOrNull(launch.id)
        if (foundLaunch != null) {
            foundLaunch.name = launch.name
            foundLaunch.details = launch.details
            foundLaunch.success = launch.success
            foundLaunch.failures = launch.failures
            foundLaunch.launchpad = launch.launchpad
            foundLaunch.capsules = launch.capsules
            db.save(foundLaunch)
        } else {
            db.save(launch)
        }
    }

}
