package com.kg.spacex.models.launch

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.launch.failure.FailureExternal
import com.kg.spacex.models.payload.PayloadExternal
import javax.persistence.*

@Entity
@Table(name = "launches")
class LaunchExternal (
    @Column
    var name: String,

    @Column(nullable = true)
    var details: String?,

    @Column
    var date_utc: String,

    @Column
    var success: Boolean,

    @Transient
    @OneToMany(mappedBy = "launchId")
    var failures: List<FailureExternal>,

    @Id
    @Column(name = "launch_id")
    val id: String,

    @ManyToOne
    @JoinColumn(name = "launchpad_id")
    var launchpad: Launchpad,

//    @Transient
//    val payloads: List<PayloadExternal>,
//
    @Transient
    var capsules: List<Capsule>
)
