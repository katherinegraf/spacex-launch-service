package com.kg.spacex.models.payload

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "payloads")
class PayloadExternal (
    @Id
    @Column(name = "payload_id")
    val id: String,

    @Column
    val name: String,

    @Column
    val type: String,

    @Column(nullable = true)
    val regime: String?,

    @Column
    val launchId: String,

    @Column
    val customers: String,

    @Column
    val nationalities: String,

    @Column
    val manufacturers: String,

    @Column
    val mass_kg: Float,

    @Column
    val mass_lbs: Float
)
