package com.kg.spacex.models.payload

import com.kg.spacex.models.launch.LaunchExternal
import javax.persistence.*

@Entity
@Table(name = "payloads")
class PayloadExternal (
    @Id
    @Column(name = "payload_id")
    val id: String,

    @Column
    var name: String,

    @Column
    var type: String,

    @Column(nullable = true)
    var regime: String?,

    @Column
    val launchId: String,

    @Column
    var customers: String,

    @Column
    var nationalities: String,

    @Column
    var manufacturers: String,

    @Column
    var mass_kg: Float,

    @Column
    var mass_lbs: Float
)
