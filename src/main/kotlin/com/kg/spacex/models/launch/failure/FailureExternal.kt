package com.kg.spacex.models.launch.failure

import com.kg.spacex.models.launch.LaunchExternal
import javax.persistence.*

@Entity
@Table(name = "launch_failures")
class FailureExternal (
    @Column
    val time: String,

    @Column(nullable = true)
    val altitude: Long?,

    @Column
    val reason: String,

    @Column
    val launchId: String
) {
    @Id
    @GeneratedValue
    val id: Long = 0
}