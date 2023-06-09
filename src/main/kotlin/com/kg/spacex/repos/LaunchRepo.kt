package com.kg.spacex.repos

import com.kg.spacex.models.launch.LaunchExternal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LaunchRepository : JpaRepository<LaunchExternal, String>
