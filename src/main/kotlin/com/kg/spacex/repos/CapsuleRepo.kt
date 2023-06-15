package com.kg.spacex.repos

import com.kg.spacex.models.capsule.CapsuleExternal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CapsuleRepository : JpaRepository<CapsuleExternal, String> {


}
