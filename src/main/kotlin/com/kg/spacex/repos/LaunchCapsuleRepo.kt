package com.kg.spacex.repos

import com.kg.spacex.models.launch.LaunchCapsule
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LaunchCapsuleRepository : CrudRepository<LaunchCapsule, String> {

    fun findByLaunchIdAndCapsuleId(launchId: String, capsuleId: String): LaunchCapsule?

    fun findByLaunchId(launchId: String): List<LaunchCapsule>?

}
