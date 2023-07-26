package com.kg.spacex.services

import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.launch.failure.FailureInternal
import com.kg.spacex.repos.FailureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FailureService() {

    @Autowired
    private lateinit var db: FailureRepository

    // TODO refactor for cleanup

    fun convertToExternal(
        failures: List<FailureInternal>,
        launchId: String
    ): List<FailureExternal> {
        val failureExternals = mutableListOf<FailureExternal>()
        failures.forEach { failure ->
            val newFailureExternal = FailureExternal(
                time = failure.time,
                altitude = failure.altitude,
                reason = failure.reason,
                launchId = launchId
            )
            failureExternals.add(newFailureExternal)
        }
        return failureExternals
    }

    fun saveOrUpdate(
        failures: List<FailureExternal>
    ) {
        failures.forEach { db.save(it) }
    }

    fun getById(
        launchId: String
    ): List<FailureExternal> {
        return db.findAllByLaunchId(launchId)
    }
}
