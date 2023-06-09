package com.kg.spacex.models.launch

import javax.persistence.*

@Entity
@Table(name = "launch_capsule_details")
class LaunchCapsuleId (
    @Column
    val launchId: String,

    @Column
    val capsuleId: String
) {
    @Id
    @GeneratedValue
    val id: Long = 0
}
