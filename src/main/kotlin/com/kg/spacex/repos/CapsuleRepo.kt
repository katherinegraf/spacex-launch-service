package com.kg.spacex.repos

import com.kg.spacex.models.Capsule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CapsuleRepository : JpaRepository<Capsule, String> {

//    fun findById(capsuleId: String): Capsule?
}
