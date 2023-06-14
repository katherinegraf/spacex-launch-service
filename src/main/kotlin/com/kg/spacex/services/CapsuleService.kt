package com.kg.spacex.services

import com.kg.spacex.models.Capsule
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchCapsuleRepository
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: CapsuleRepository

    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    fun fetchOneCapsule(
        capsuleId: String
    ): Capsule? {
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL.plus(capsuleId),
            deserializer = Capsule.Deserializer()
        ) ?: return null
        val capsule = apiResult as Capsule
        updateOrSaveCapsule(capsule)
        return (capsule)
    }

    fun fetchAndSaveAllCapsules(): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        val apiResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL,
            deserializer = Capsule.ArrayDeserializer()
        ) as Array<*>? ?: return null
        apiResult.forEach { c ->
            c as Capsule
            updateOrSaveCapsule(c)
            capsules.add(c)
        }
        return capsules
    }

    fun updateOrSaveCapsule(
        capsule: Capsule
    ) {
        val foundCapsule = db.findByIdOrNull(capsule.id)
        if (foundCapsule != null) {
            foundCapsule.status = capsule.status
            foundCapsule.last_update = capsule.last_update
            foundCapsule.water_landings = capsule.water_landings
            foundCapsule.land_landings = capsule.land_landings
            db.save(foundCapsule)
        } else {
            db.save(capsule)
        }
    }

    fun getCapsulesById(
        capsuleIds: List<String>
    ): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        capsuleIds.forEach { id ->
            val foundCapsule = db.findByIdOrNull(id) ?: return null
            capsules.add(foundCapsule)
        }
        return capsules
    }

    fun getCapsulesForLaunch(
        launchId: String
    ): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        val foundLaunchCapsuleDetails = launchCapsuleRepo.findByLaunchId(launchId) ?: return null
        foundLaunchCapsuleDetails.forEach { launchCapsule ->
            val capsule = db.findByIdOrNull(launchCapsule.capsuleId) ?: return null
            capsules.add(capsule)
        }
        return capsules
    }
}
