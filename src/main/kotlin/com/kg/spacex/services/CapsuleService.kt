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
import java.util.logging.Logger

@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: CapsuleRepository

    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    val logger = Logger.getLogger("logger")

    // TODO get rid of fetchOne methods? Only used in testing.
    fun fetchOneCapsule(
        capsuleId: String
    ): Boolean {
        val result = makeAPICall(capsuleId) as CapsuleInternal?
        return result != null
    }

    fun fetchAllCapsules(): List<CapsuleInternal>? {
        val resultList = makeAPICall(null) as Array<*>? ?: return null
        val capsules = mutableListOf<CapsuleInternal>()
        resultList.forEach { result ->
            result as CapsuleInternal
            capsules.add(result)
        }
        return capsules
    }

    fun makeAPICall(
        capsuleId: String?
    ): Any? {
        return if (capsuleId != null) {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL.plus(capsuleId),
                deserializer = CapsuleInternal.Deserializer()
            )
        } else {
            spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL,
                deserializer = CapsuleInternal.ArrayDeserializer()
            )
        }
    }

    fun updateOrSaveCapsules(
        capsules: List<CapsuleInternal>
    ) {
        capsules.forEach { capsule ->
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
    ): List<CapsuleExternal> {
        val capsules = mutableListOf<CapsuleExternal>()
        val foundJoinRecords = launchCapsuleRepo.findAllByLaunchId(launchId)
        return if (foundJoinRecords.isEmpty()) {
            emptyList()
        } else {
            foundJoinRecords.forEach { joinRecord ->
                val foundCapsule = db.findByIdOrNull(joinRecord.capsuleId) ?: run {
                    logger.warning("Capsule ${joinRecord.capsuleId} not found in capsule repo, " +
                            "yet exists in launch capsule repo.")
                }
                capsules.add(foundCapsule as CapsuleExternal)
            }
            capsules
        }
    }
}
