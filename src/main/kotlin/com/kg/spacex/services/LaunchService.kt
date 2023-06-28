package com.kg.spacex.services

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.utils.SPACEX_API_LAUNCHES_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.util.logging.Logger

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

    val logger = Logger.getLogger("logger")

    fun fetchAllData(): Any? {
        val capsules = capsuleService.fetchAllCapsules()
        if (capsules == null) {
            logger.warning("Failed to fetch capsules")
            return null
        }
        val launchpads = launchpadService.fetchAllLaunchpads()
        if (launchpads == null) {
            logger.warning("Failed to fetch launchpads")
            return null
        }
        val launches = fetchAllLaunches()
        if (launches == null) {
            logger.warning("Failed to fetch launches")
            return null
        }
        val payloads = payloadService.fetchAllPayloads()
        if (payloads == null) {
            logger.warning("Failed to fetch payloads")
            return null
        }
        capsuleService.updateOrSaveCapsules(capsules)
        launchpadService.updateOrSaveLaunchpads(launchpads)
        updateOrSaveLaunches(launches)
        payloadService.updateOrSavePayloads(payloads)
        return true
    }

    fun fetchAllLaunches(): List<LaunchInternal>? {
        val resultList = makeAPICall(null) as Array<*>? ?: return null
        val launches = mutableListOf<LaunchInternal>()
        resultList.forEach { result ->
            result as LaunchInternal
            launches.add(result)
        }
        return launches
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
            logger.warning("Failed to find launchpad ${launchInternal.launchpadId} for launch ${launchInternal.id}")
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

    fun updateOrSaveLaunches(
        launches: List<LaunchInternal>
    ): Any? {
        launches.forEach { launch ->
            val launchExternal = prepareToSaveLaunch(launch) ?: run {
                logger.warning("Unable to prepareToSaveLaunch for launch ${launch.id}")
                return null
            }
            val foundLaunch = db.findByIdOrNull(launchExternal.id)
            if (foundLaunch != null) {
                foundLaunch.name = launchExternal.name
                foundLaunch.details = launchExternal.details
                foundLaunch.success = launchExternal.success
                foundLaunch.failures = launchExternal.failures
                foundLaunch.launchpad = launchExternal.launchpad
                foundLaunch.updated_at = launchExternal.updated_at
                db.save(foundLaunch)
                failureService.updateOrSaveFailures(foundLaunch.failures)
            } else {
                db.save(launchExternal)
                failureService.updateOrSaveFailures(launchExternal.failures)
            }
        }
        return true
    }

    fun getAllLaunchesFromDb(): List<LaunchExternal>? {
        val foundLaunches = db.findAllByOrderById()
        val launches = mutableListOf<LaunchExternal>()
        foundLaunches.forEach { launch ->
            val launchExternal = getLaunchExternalFromDbById(launch.id) ?: return null
            launches.add(launchExternal)
        }
        return launches
    }

    fun getLaunchExternalFromDbById(
        launchId: String
    ): LaunchExternal? {
        val found = db.findByIdOrNull(launchId) ?: run {
            logger.warning("Failed to find launch $launchId")
            return null
        }
        val failures = failureService.getFailuresById(launchId)
        val payloads = payloadService.getPayloadsByLaunchId(launchId)
        val capsules = capsuleService.getCapsulesForLaunch(launchId)
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
        val lastUpdated = db.findFirstByOrderById()?.updated_at ?: return true
        val today = LocalDate.now()
        val daysSinceLastUpdate = Period.between(lastUpdated, today).days
        return daysSinceLastUpdate > 6
    }

}
