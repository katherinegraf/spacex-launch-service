package com.kg.spacex.models.launch

import javax.persistence.*

@Entity
@Table(name = "launch_capsule_details")
class LaunchCapsule (
    @Column
    val launchId: String,

    @Column
    val capsuleId: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
