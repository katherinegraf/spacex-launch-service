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
    val name: String,

    @Column(nullable = true)
    val details: String?,

    @Column
    val date_utc: String,

    @Column
    val success: Boolean,

    @OneToMany(mappedBy = "launchId")
    val failures: List<FailureExternal>,

    @Id
    @Column(name = "launch_id")
    val id: String,

    @ManyToOne
    @JoinColumn(name = "fk_launchpad")
    val launchpad: Launchpad,

    @Transient
    val payloads: List<PayloadExternal>,

    @Transient

    val capsules: List<Capsule>
)
