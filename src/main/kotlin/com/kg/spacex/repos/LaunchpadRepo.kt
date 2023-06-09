package com.kg.spacex.repos

import com.kg.spacex.models.Launchpad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LaunchpadRepository : JpaRepository<Launchpad, String> {

}
