package com.kg.spacex.repos

import com.kg.spacex.models.launch.failure.FailureExternal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FailureRepository : JpaRepository<FailureExternal, Long> {

    fun findByLaunchId(launchId: String): FailureExternal?

    fun findAllByLaunchId(launchId: String): List<FailureExternal>

}
