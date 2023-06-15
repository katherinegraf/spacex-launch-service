package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class LaunchpadService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: LaunchpadRepository

    fun fetchOneLaunchpad(
        launchpadId: String
    ) {
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHPADS_URL.plus(launchpadId),
            deserializer = Launchpad.Deserializer()
        )
        val launchpad = apiResult as Launchpad
        updateOrSaveLaunchpad(launchpad)
    }

    fun fetchAndSaveAllLaunchpads() {
        val launchpads = mutableListOf<Launchpad>()
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHPADS_URL,
            deserializer = Launchpad.ArrayDeserializer()
        ) as Array<*>?
        apiResult?.forEach { launchpad ->
            launchpad as Launchpad
            updateOrSaveLaunchpad(launchpad)
            launchpads.add(launchpad)
        }
    }

    fun updateOrSaveLaunchpad(
        launchpad: Launchpad
    ) {
        val foundLaunchpad = db.findByIdOrNull(launchpad.id)
        if (foundLaunchpad != null) {
            foundLaunchpad.status = launchpad.status
            foundLaunchpad.details = launchpad.details
            foundLaunchpad.launch_attempts = launchpad.launch_attempts
            foundLaunchpad.launch_successes = launchpad.launch_successes
            db.save(foundLaunchpad)
        } else {
            db.save(launchpad)
        }
    }

    fun getLaunchpadById(
        launchpadId: String
    ): Launchpad? {
        return db.findByIdOrNull(launchpadId)
    }
}
