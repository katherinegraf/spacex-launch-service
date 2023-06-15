package com.kg.spacex.models.launch

import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.payload.PayloadExternal
import javax.persistence.*

@Entity
@Table(name = "launches")
class LaunchExternal (

    @Id
    @Column(name = "launch_id")
    val id: String,

    @Column
    var name: String,

    @Column(nullable = true)
    var details: String?,

    @Column
    var date_utc: String,

    @Column(nullable = true)
    var success: Boolean?,

    @Transient
    var failures: List<FailureExternal>,

    @ManyToOne
    @JoinColumn(name = "launchpad_id")
    var launchpad: Launchpad,

    @Transient
    var payloads: List<PayloadExternal>,

    @Transient
    var capsules: List<CapsuleExternal>
)
