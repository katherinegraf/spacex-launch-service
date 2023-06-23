package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class LaunchpadService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: LaunchpadRepository

    val logger = Logger.getLogger("logger")

    fun fetchOneLaunchpad(
        launchpadId: String
    ): Boolean {
        val result = makeAPICall(launchpadId) as Launchpad?
        return result != null
    }

    fun fetchAllLaunchpads(): Boolean {
        val resultList = makeAPICall(null) as Array<*>?
        return !(resultList == null || resultList.isEmpty())
    }

    fun makeAPICall(
        launchpadID: String?
    ): Any? {
        return if (launchpadID != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHPADS_URL.plus(launchpadID),
                deserializer = Launchpad.Deserializer()
            )
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_LAUNCHPADS_URL,
                deserializer = Launchpad.ArrayDeserializer()
            )
        }
    }

    fun updateOrSaveLaunchpads(
        launchpads: List<Launchpad>
    ) {
        launchpads.forEach { launchpad ->
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
    }

    fun getLaunchpadById(
        launchpadId: String
    ): Launchpad? {
        return db.findByIdOrNull(launchpadId)
    }
}
