package com.kg.spacex.services

import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.launch.failure.FailureInternal
import com.kg.spacex.repos.FailureRepository
import org.springframework.stereotype.Service

@Service
class FailureService (private var repo: FailureRepository) {

    /**
     * Converts FailureInternals to FailureExternals by adding on a launchId attribute
     */
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
        failures.forEach { repo.save(it) }
    }

    fun getAllByLaunchId(
        launchId: String
    ): List<FailureExternal> {
        return repo.findAllByLaunchId(launchId)
    }
}
