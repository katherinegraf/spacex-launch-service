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
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.Period
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
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    val logger = Logger.getLogger("logger")

    fun fetchAllData() {
        capsuleService.fetchAndSaveAllCapsules()
        launchpadService.fetchAndSaveAllLaunchpads()
        fetchAndSaveAllLaunches()
        payloadService.fetchAndSaveAllPayloads()
    }

    // TODO new process flow won't need a fetchOne method
    fun fetchAndSaveOneLaunch(
        launchId: String
    ) {
        val result = makeAPICall(launchId) as LaunchInternal
        val newLaunchExternal = prepareToSaveLaunch(result)
        if (newLaunchExternal != null) {
            updateOrSaveLaunch(newLaunchExternal)
        }
    }

    fun fetchAndSaveAllLaunches() {
        val resultList = makeAPICall(null) as Array<*>
        resultList.forEach { result ->
            val newLaunchExternal = prepareToSaveLaunch(result as LaunchInternal)
            if (newLaunchExternal != null) {
                updateOrSaveLaunch(newLaunchExternal)
            }
        }
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

    fun makeAPICall(
        launchId: String?
    ): Any {
        return if (launchId != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHES_URL.plus(launchId),
                deserializer = LaunchInternal.Deserializer()
            ) as LaunchInternal
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHES_URL,
                deserializer = LaunchInternal.ArrayDeserializer()
            ) as Array<*>
        }
    }

    fun prepareToSaveLaunch(
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
        return LaunchExternal(
            name = launchInternal.name,
            details = launchInternal.details,
            date_utc = launchInternal.date_utc,
            success = launchInternal.success,
            failures = failureExternals,
            id = launchInternal.id,
            launchpad = launchpad,
            payloads = emptyList(),
            capsules = emptyList(),
            updated_at = LocalDate.now()
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
            foundLaunch.updated_at = launch.updated_at
            db.save(foundLaunch)
            failureService.updateOrSaveFailures(foundLaunch.failures)
        } else {
            db.save(launch)
            failureService.updateOrSaveFailures(launch.failures)
        }
    }

    fun buildLaunchExternalFromDbById(
        launchId: String
    ): LaunchExternal? {
        val found = db.findByIdOrNull(launchId) ?: run {
            logger.warning("Failed to find launch $launchId.")
            return null
        }
        val failures = failureService.getFailuresById(launchId) ?: run {
            logger.warning("Failed to find failures for launch $launchId.")
            return null
        }
        val payloads = payloadService.getPayloadsByLaunchId(launchId) ?: run {
            logger.warning("Failed to find payloads for launch $launchId.")
            return null
        }
        val capsules = capsuleService.getCapsulesForLaunch(launchId) ?: run {
            logger.warning("Failed to find capsules for launch $launchId.")
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
            capsules = capsules,
            updated_at = found.updated_at
        )
    }

    fun isDataRefreshNeeded(): Boolean {
        val lastUpdated = db.findFirstByOrderById().updated_at
        val today = LocalDate.now()
        val daysSinceLastUpdate = Period.between(lastUpdated, today).days
        return daysSinceLastUpdate > 6
    }

}
