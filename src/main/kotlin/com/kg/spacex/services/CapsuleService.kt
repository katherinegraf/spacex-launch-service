package com.kg.spacex.services

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.launch.LaunchCapsule
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchCapsuleRepository
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: CapsuleRepository

    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    fun fetchAndSaveOneCapsule(
        capsuleId: String
    ) {
        val result = makeAPICall(capsuleId) as CapsuleInternal
        updateOrSaveCapsule(result)
    }

    fun fetchAndSaveAllCapsules() {
        val resultList = makeAPICall(null) as Array<*>
        resultList.forEach { result ->
            result as CapsuleInternal
            updateOrSaveCapsule(result)
        }
    }

    fun makeAPICall(
        capsuleId: String?
    ): Any {
        return if (capsuleId != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL.plus(capsuleId),
                deserializer = CapsuleInternal.Deserializer()
            ) as CapsuleInternal
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL,
                deserializer = CapsuleInternal.ArrayDeserializer()
            ) as Array<*>
        }
    }

    fun prepareToSaveCapsule(
        capsule: CapsuleInternal
    ): CapsuleExternal {
        return CapsuleExternal(
            id = capsule.id,
            serial = capsule.serial,
            type = capsule.type,
            status = capsule.status,
            last_update = capsule.last_update,
            water_landings = capsule.water_landings,
            land_landings = capsule.land_landings
        )
    }

    fun updateOrSaveCapsule(
        capsule: CapsuleInternal
    ) {
        val foundCapsule = db.findByIdOrNull(capsule.id)
        if (foundCapsule != null) {
            foundCapsule.status = capsule.status
            foundCapsule.last_update = capsule.last_update
            foundCapsule.water_landings = capsule.water_landings
            foundCapsule.land_landings = capsule.land_landings
            db.save(foundCapsule)
        } else {
            val newCapsuleExternal = prepareToSaveCapsule(capsule)
            db.save(newCapsuleExternal)
        }
        updateLaunchCapsuleJoinTable(capsule)
    }

    fun updateLaunchCapsuleJoinTable(
        capsule: CapsuleInternal
    ) {
        val launchIds = capsule.launchIds
        launchIds.forEach { launchId ->
            val foundMatch = launchCapsuleRepo.findByLaunchIdAndCapsuleId(launchId, capsule.id)
            if (foundMatch == null) {
                launchCapsuleRepo.save(
                    LaunchCapsule(
                        launchId = launchId,
                        capsuleId = capsule.id
                    )
                )
            }
        }
    }

    fun getCapsulesById(
        capsuleIds: List<String>
    ): List<CapsuleExternal>? {
        val capsules = mutableListOf<CapsuleExternal>()
        capsuleIds.forEach { id ->
            val foundCapsule = db.findByIdOrNull(id) ?: return null
            capsules.add(foundCapsule)
        }
        return capsules
    }

    fun getCapsulesForLaunch(
        launchId: String
    ): List<CapsuleExternal>? {
        val capsules = mutableListOf<CapsuleExternal>()
        val foundLaunchCapsuleDetails = launchCapsuleRepo.findByLaunchId(launchId) ?: return null
        foundLaunchCapsuleDetails.forEach { launchCapsule ->
            val capsule = db.findByIdOrNull(launchCapsule.capsuleId) ?: return null
            capsules.add(capsule)
        }
        return capsules
    }
}
