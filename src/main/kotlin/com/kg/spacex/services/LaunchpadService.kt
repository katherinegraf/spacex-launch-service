package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LaunchpadService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: LaunchpadRepository

    fun fetchOneLaunchpad(
        launchpadId: String
    ) {
        val result = makeAPICall(launchpadId) as Launchpad
        updateOrSaveLaunchpad(result)
    }

    fun fetchAndSaveAllLaunchpads() {
        val resultList = makeAPICall(null) as Array<*>
        resultList.forEach { result ->
            result as Launchpad
            updateOrSaveLaunchpad(result)
        }
    }

    fun makeAPICall(
        launchpadID: String?
    ): Any {
        return if (launchpadID != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHPADS_URL.plus(launchpadID),
                deserializer = Launchpad.Deserializer()
            ) as Launchpad
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHPADS_URL,
                deserializer = Launchpad.ArrayDeserializer()
            ) as Array<*>
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
