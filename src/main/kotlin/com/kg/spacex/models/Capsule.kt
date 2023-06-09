package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.kg.spacex.models.payload.PayloadInternal
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
    val type: String,

    @Column
    var status: String,

    @Column
    var last_update: String?,

    @Column(nullable = true)
    var water_landings: Long? = 0,

    @Column(nullable = true)
    var land_landings: Long? = 0,
) {
    class Deserializer : ResponseDeserializable<Capsule> {
        override fun deserialize(content: String): Capsule =
            Gson().fromJson(content, Capsule::class.java)
    }
    class ArrayDeserializer : ResponseDeserializable<Array<Capsule>> {
        override fun deserialize(content: String): Array<Capsule> =
            Gson().fromJson(content, Array<Capsule>::class.java)
    }
}
