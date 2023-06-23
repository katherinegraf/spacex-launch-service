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
import kotlin.math.log

@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: CapsuleRepository

    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    val logger = Logger.getLogger("logger")

    fun fetchOneCapsule(
        capsuleId: String
    ): Boolean {
        val result = makeAPICall(capsuleId) as CapsuleInternal?
        return result != null
    }

    fun fetchAllCapsules(): Boolean {
        val resultList = makeAPICall(null) as Array<*>?
        return !(resultList == null || resultList.isEmpty())
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
    ): List<CapsuleExternal>? {
        val capsules = mutableListOf<CapsuleExternal>()
        val foundMatches = launchCapsuleRepo.findAllByLaunchId(launchId)
        return if (foundMatches.isEmpty()) {
            logger.warning("Match not found in launch capsule repo")
            null
        } else {
            foundMatches.forEach { launchCapsule ->
                val foundCapsule = db.findByIdOrNull(launchCapsule.capsuleId) ?: run {
                    logger.warning("Capsule ${launchCapsule.capsuleId} not found in capsule repo.")
                    return null
                }
                capsules.add(foundCapsule)
            }
            capsules
        }
    }
}
