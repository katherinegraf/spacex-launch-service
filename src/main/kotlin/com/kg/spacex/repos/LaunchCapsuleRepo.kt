package com.kg.spacex.repos

import com.kg.spacex.models.launch.LaunchCapsuleId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LaunchCapsuleRepository : CrudRepository<LaunchCapsuleId, String> {

}
