package com.kg.spacex.services

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import com.kg.spacex.utils.SPACEX_API_PAYLOADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Connects to [spaceXAPIService] to make call to SpaceX API.
 */
@Service
class CapsuleService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: CapsuleRepository

    /**
     * Fetches a list of Capsules that match the provided id(s).
     *
     * @param capsuleIds is a list of ids for capsules.
     * @return a list of Capsules or null, if the API call fails or returns null.
     */
    fun fetchCapsules(
        capsuleIds: List<String>
    ): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        capsuleIds.forEach { id ->
            val apiResult = spaceXAPIService.handleAPICall(
                url = SPACEX_API_CAPSULES_URL.plus(id),
                deserializer = Capsule.Deserializer()
            ) ?: return null
            capsules.add(apiResult as Capsule)
        }
        return capsules
    }

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

    fun findCapsulesById(
        capsuleIds: List<String>
    ): List<Capsule>? {
        val capsules = mutableListOf<Capsule>()
        capsuleIds.forEach { id ->
            val foundCapsule = db.findByIdOrNull(id) ?: return null
            capsules.add(foundCapsule)
        }
        return capsules
    }
}
