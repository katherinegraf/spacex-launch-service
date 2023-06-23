package com.kg.spacex.repos

import com.kg.spacex.models.payload.PayloadExternal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PayloadRepository : JpaRepository<PayloadExternal, String> {

    fun findAllByLaunchId(launchId: String): List<PayloadExternal>
}