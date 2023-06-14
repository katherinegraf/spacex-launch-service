package com.kg.spacex.services

import com.kg.spacex.models.launch.LaunchCapsule
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.repos.LaunchCapsuleRepository
import com.kg.spacex.repos.LaunchRepository
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

//    @Autowired
//    private lateinit var launchpadRepo: LaunchpadRepository
//
//    @Autowired
//    private lateinit var capsuleRepo: CapsuleRepository
//
    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    val logger = Logger.getLogger("logger")

    fun fetchAndSaveOneLaunch(
        launchId: String
    ): LaunchExternal? {
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL.plus(launchId),
            deserializer = LaunchInternal.Deserializer()
        ) ?: return null
        val launchInternal = apiResult as LaunchInternal
        val newLaunchExternal = externalizeLaunch(launchInternal) ?: return null
        updateOrSaveLaunch(newLaunchExternal)
        return newLaunchExternal
    }

    fun fetchAndSaveAllLaunches(): List<LaunchExternal>? {
        val launches = mutableListOf<LaunchExternal>()
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL,
            deserializer = LaunchInternal.ArrayDeserializer()
        ) as Array<*>? ?: return null
        apiResult.forEach { launchInternal ->
            launchInternal as LaunchInternal
            val newLaunchExternal = externalizeLaunch(launchInternal) ?: return null
            updateOrSaveLaunch(newLaunchExternal)
            launches.add(newLaunchExternal)
        }
        return launches
    }

    fun getAllLaunchesFromDb(): List<LaunchExternal>? {
        val foundLaunches = db.findAllByOrderById()
        val launches = mutableListOf<LaunchExternal>()
        foundLaunches.forEach { launch ->
            val launchExternal = buildLaunchExternalFromDbById(launch.id) ?: return null
            launches.add(launchExternal)
        }
        return launches
    }

    fun getOneLaunchFromDb(
        launchId: String
    ): LaunchExternal? {
        return db.findByIdOrNull(launchId)
    }

    fun externalizeLaunch(
        launchInternal: LaunchInternal
    ): LaunchExternal? {
        val failureExternals = failureService.externalizeFailure(
            launchInternal.failures,
            launchInternal.id
        )
        val launchpad = launchpadService.getLaunchpadById(launchInternal.launchpadId) ?: run {
            logger.warning("Failed to retrieve launchpad ${launchInternal.launchpadId}")
            return null
        }
        val payloads = payloadService.getPayloadsByLaunchId(launchInternal.id) ?: run {
            logger.warning("Failed to retrieve payloads for launch ${launchInternal.id}")
            return null
        }
        val capsules = capsuleService.getCapsulesById(launchInternal.capsuleIds) ?: run {
            logger.warning("Failed to retrieve capsules ${launchInternal.capsuleIds}")
            return null
        }
        return LaunchExternal(
            name = launchInternal.name,
            details = launchInternal.details,
            date_utc = launchInternal.date_utc,
            success = launchInternal.success,
            failures = failureExternals,
            id = launchInternal.id,
            launchpad = launchpad,
            payloads = payloads,
            capsules = capsules
        )
    }

    fun buildLaunchExternalFromDbById(
        launchId: String
    ): LaunchExternal? {
        val found = db.findByIdOrNull(launchId) ?: run {
            logger.warning("Failed to find launch ${launchId}.")
            return null
        }
        val failures = failureService.getFailuresById(launchId) ?: run {
            logger.warning("Failed to find failures for launch ${launchId}.")
            return null
        }
        val payloads = payloadService.getPayloadsByLaunchId(launchId) ?: run {
            logger.warning("Failed to find payloads for launch ${launchId}.")
            return null
        }
        val capsules = capsuleService.getCapsulesForLaunch(launchId) ?: run {
            logger.warning("Failed to find capsules for launch ${launchId}.")
            return null
        }
        return LaunchExternal(
            id = found.id,
            name = found.name,
            details = found.details,
            date_utc = found.date_utc,
            success = found.success,
            failures = failures,
            launchpad = found.launchpad,
            payloads = payloads,
            capsules = capsules
        )
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
            foundLaunch.payloads = launch.payloads
            foundLaunch.capsules = launch.capsules
            db.save(foundLaunch)
            updateLaunchCapsuleJoinTable(foundLaunch)
            failureService.updateOrSaveFailures(foundLaunch.failures)
        } else {
            db.save(launch)
            updateLaunchCapsuleJoinTable(launch)
            failureService.updateOrSaveFailures(launch.failures)
        }
    }

    fun updateLaunchCapsuleJoinTable(
        launch: LaunchExternal
    ) {
        val capsules = launch.capsules
        capsules.forEach { capsule ->
            val foundMatch = launchCapsuleRepo.findByLaunchIdAndCapsuleId(launch.id, capsule.id)
            if (foundMatch == null) {
                launchCapsuleRepo.save(
                    LaunchCapsule(
                        launchId = launch.id,
                        capsuleId = capsule.id
                    )
                )
            }
        }
    }

}
