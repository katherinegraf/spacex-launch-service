package com.kg.spacex.services

import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class LaunchService @Autowired constructor (
    private val capsuleService: CapsuleService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService,
    private val spaceXAPIService: SpaceXAPIService,
    private val failureService: FailureService,
    private val db: LaunchRepository
) {

    fun refreshAllData() {
        val capsules = capsuleService.fetchAll()
        val launchpads = launchpadService.fetchAll()
        val launches = fetchAll()
        val payloads = payloadService.fetchAll()
        capsuleService.saveOrUpdate(capsules)
        launchpadService.saveOrUpdate(launchpads)
        saveOrUpdate(launches)
        payloadService.saveOrUpdate(payloads)
    }

    fun fetchOne(
        launchId: String
    ): LaunchInternal {
        val result = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL.plus(launchId),
            deserializer = LaunchInternal.Deserializer()
        ) as LaunchInternal?
        return result ?: throw ResourceUnavailableException()
    }

    fun fetchAll(): List<LaunchInternal> {
        val launches = mutableListOf<LaunchInternal>()
        val resultList = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHES_URL,
            deserializer = LaunchInternal.ArrayDeserializer()
        ) as Array<*>? ?: throw ResourceUnavailableException()
        resultList.forEach { launches.add(it as LaunchInternal) }
        return launches
    }

    fun saveOrUpdate(
        launches: List<LaunchInternal>
    ) {
        launches.forEach { launch ->
            val launchExternal = convertToExternal(launch)
                db.save(launchExternal)
                failureService.saveOrUpdate(launchExternal.failures)
        }
    }

    /**
     * Converts a LaunchInternal to a LaunchExternal in preparation to save LaunchExternal by:
     *  - converting failures from FailureInternal to FailureExternal
     *  - converting from launchpadId: String to full Launchpad object
     *  - setting payloads attribute to an empty list of PayloadExternal,
     *      since PayloadExternals is only a @Transient attribute (not saved in launch table)
     *  - setting capsules attribute to an empty list of CapsuleExternal,
     *      since CapsuleExternals is only a @Transient attribute (not saved in launch table)
     */
    fun convertToExternal(
        launchInternal: LaunchInternal
    ): LaunchExternal {
        val failureExternals = failureService.convertToExternal(
            launchInternal.failures,
            launchInternal.id
        )
        val launchpad = launchpadService.getById(launchInternal.launchpadId)
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

    fun getAll(): List<LaunchExternal> {
        if (isDataRefreshNeeded()) refreshAllData()
        val launches = mutableListOf<LaunchExternal>()
        val foundLaunches = db.findAllByOrderById()
        foundLaunches.forEach { launch ->
            val newLaunchExternal = buildLaunchExternalById(launch.id)
            launches.add(newLaunchExternal)
        }
        return launches
    }

    fun buildLaunchExternalById(
        launchId: String
    ): LaunchExternal {
        val foundLaunch = db.findByIdOrNull(launchId) ?: throw ResourceNotFoundException()
        val failures = failureService.getAllByLaunchId(launchId)
        val payloads = payloadService.getAllByLaunchId(launchId)
        val capsules = capsuleService.getAllByLaunchId(launchId)
        return LaunchExternal(
            id = foundLaunch.id,
            name = foundLaunch.name,
            details = foundLaunch.details,
            date_utc = foundLaunch.date_utc,
            success = foundLaunch.success,
            failures = failures,
            launchpad = foundLaunch.launchpad,
            payloads = payloads,
            capsules = capsules,
            updated_at = foundLaunch.updated_at
        )
    }

    fun isDataRefreshNeeded(): Boolean {
        val lastUpdated = db.findFirstByOrderById()?.updated_at ?: return true
        val daysSinceLastUpdate = Period.between(lastUpdated, LocalDate.now()).days
        return daysSinceLastUpdate > 6
    }
}
