package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class LaunchpadService (private val spaceXAPIService: SpaceXAPIService) {

    @Autowired
    private lateinit var db: LaunchpadRepository

    fun fetchOne(
        id: String
    ): Launchpad {
        val result = spaceXAPIService.handleAPICall(
            SPACEX_API_LAUNCHPADS_URL.plus(id),
            Launchpad.Deserializer()
        ) as Launchpad?
        return result ?: throw ResourceUnavailableException()
    }

    fun fetchAll(): List<Launchpad> {
        val launchpads = mutableListOf<Launchpad>()
        val resultList = spaceXAPIService.handleAPICall(
            url = SPACEX_API_LAUNCHPADS_URL,
            deserializer = Launchpad.ArrayDeserializer()
        ) as Array<*>? ?: throw ResourceUnavailableException()
        resultList.forEach { launchpads.add(it as Launchpad) }
        return launchpads
    }

    fun saveOrUpdate(
        launchpads: List<Launchpad>
    ) {
        launchpads.forEach { db.save(it) }
    }

    fun getById(
        launchpadId: String
    ): Launchpad {
        return db.findByIdOrNull(launchpadId) ?: throw ResourceNotFoundException()
    }
}
