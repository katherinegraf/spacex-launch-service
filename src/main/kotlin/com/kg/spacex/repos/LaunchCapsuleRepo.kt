package com.kg.spacex.repos

import com.kg.spacex.models.launch.LaunchCapsule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LaunchCapsuleRepository : JpaRepository<LaunchCapsule, String> {

    fun findByLaunchIdAndCapsuleId(launchId: String, capsuleId: String): LaunchCapsule?

    fun findAllByLaunchId(launchId: String): List<LaunchCapsule>
}
