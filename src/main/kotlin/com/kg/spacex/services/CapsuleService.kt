package com.kg.spacex.services

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.launch.LaunchCapsule
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchCapsuleRepository
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CapsuleService (
    private val spaceXAPIService: SpaceXAPIService,
    private var repo: CapsuleRepository,
    private var joinRepo: LaunchCapsuleRepository
) {

    fun fetchOne(
        id: String
    ): CapsuleInternal {
        val result = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL.plus(id),
            deserializer = CapsuleInternal.Deserializer()
        ) as CapsuleInternal?
        return result ?: throw ResourceUnavailableException()
    }

    fun fetchAll(): List<CapsuleInternal> {
        val capsules = mutableListOf<CapsuleInternal>()
        val resultList = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL,
            deserializer = CapsuleInternal.ArrayDeserializer()
        ) as Array<*>? ?: throw ResourceUnavailableException()
        resultList.forEach { capsules.add(it as CapsuleInternal) }
        return capsules
    }

    fun saveOrUpdate(
        capsules: List<CapsuleInternal>
    ) {
        capsules.forEach { capsule ->
            val capsuleExternal = convertToExternal(capsule)
            repo.save(capsuleExternal)
            saveJoinRecord(capsule)
        }
    }

    /**
    Converts CapsuleInternal to CapsuleExternal by copying over all attributes except launchIds.
     */
    fun convertToExternal(
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

    fun saveJoinRecord(
        capsule: CapsuleInternal
    ) {
        val launchIds = capsule.launchIds
        launchIds.forEach { launchId ->
            val existingJoinRecord = joinRepo.findByLaunchIdAndCapsuleId(launchId, capsule.id)
            if (existingJoinRecord == null) {
                joinRepo.save(
                    LaunchCapsule(
                        launchId = launchId,
                        capsuleId = capsule.id
                    )
                )
            }
        }
    }

    fun getById(
        capsuleId: String
    ): CapsuleExternal {
        return repo.findByIdOrNull(capsuleId) ?: throw ResourceNotFoundException()
    }

    fun getAllByLaunchId(
        launchId: String
    ): List<CapsuleExternal> {
        val capsules = mutableListOf<CapsuleExternal>()
        val foundJoinRecords = joinRepo.findAllByLaunchId(launchId)
        return if (foundJoinRecords.isEmpty()) {
            emptyList()
        } else {
            foundJoinRecords.forEach { joinRecord ->
                val foundCapsule = repo.findByIdOrNull(joinRecord.capsuleId)
                if (foundCapsule != null) capsules.add(foundCapsule)
            }
            capsules
        }
    }
}
