package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "capsules")
class Capsule (
    @Column(name = "serial_name")
    val serial: String,

    @Id
    @Column(name = "capsule_id")
    val id: String,

    @Column
    val status: String,

    @Column
    val last_update: String,

    @Column(nullable = true)
    val water_landings: Long? = 0,

    @Column(nullable = true)
    val land_landings: Long? = 0,
) {
    class Deserializer : ResponseDeserializable<Capsule> {
        override fun deserialize(content: String): Capsule =
            Gson().fromJson(content, Capsule::class.java)
    }
}
