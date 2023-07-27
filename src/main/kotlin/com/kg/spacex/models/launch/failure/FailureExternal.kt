package com.kg.spacex.models.launch.failure

import javax.persistence.*

@Entity
@Table(name = "launch_failures")
class FailureExternal (
    @Column
    var time: Long,

    @Column(nullable = true)
    var altitude: Long?,

    @Column
    var reason: String,

    @Column
    val launchId: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
