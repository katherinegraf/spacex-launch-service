package com.kg.spacex.models.capsule

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "capsules")
class CapsuleExternal (
    @Id
    @Column(name = "capsule_id")
    val id: String,

    @Column(name = "serial_name")
    val serial: String,

    @Column
    val type: String,

    @Column
    var status: String,

    @Column
    var last_update: String?,

    @Column(nullable = true)
    var water_landings: Long? = 0,

    @Column(nullable = true)
    var land_landings: Long? = 0,

)