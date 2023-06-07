package com.kg.spacex.services

import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.launch.failure.FailureInternal
import org.springframework.stereotype.Service

@Service
class FailureService() {

    fun convertFailureInternalToFailureExternal(
        failures: List<FailureInternal>,
        launchId: String
    ): List<FailureExternal> {
        val failureExternals = mutableListOf<FailureExternal>()
        failures.forEach { failure ->
            failureExternals.add(
                FailureExternal(
                    time = failure.time,
                    altitude = failure.altitude,
                    reason = failure.reason,
                    launchId = launchId
                )
            )
        }
        return failureExternals
    }
}
