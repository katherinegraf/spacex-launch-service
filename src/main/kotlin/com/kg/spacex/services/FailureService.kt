package com.kg.spacex.services

import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.launch.failure.FailureInternal
import org.springframework.stereotype.Service

@Service
class FailureService {

    fun convertFailureInternalToFailureExternal(
        failure: FailureInternal,
        launchId: String
    ): FailureExternal {
        return FailureExternal(
            time = failure.time,
            altitude = failure.altitude,
            reason = failure.reason,
            launchId = launchId
        )
    }
}
