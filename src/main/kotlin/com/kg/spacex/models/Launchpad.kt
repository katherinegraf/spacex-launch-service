package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import javax.persistence.*

@Entity
@Table(name = "launchpads")
class Launchpad (
    @Id
    @Column(name = "launchpad_id")
    val id: String,

    @Column
    val full_name: String,

    @Column
    val locality: String,

    @Column
    val region: String,

    @Column
    val status: String,

    @Column
    val details: String,

    @Column
    val launch_attempts: Long,

    @Column
    val launch_successes: Long,
) {
    class Deserializer : ResponseDeserializable<Launchpad> {
        override fun deserialize(content: String): Launchpad =
            Gson().fromJson(content, Launchpad::class.java)
    }
    class ArrayDeserializer : ResponseDeserializable<Array<Launchpad>> {
        override fun deserialize(content: String): Array<Launchpad> =
            Gson().fromJson(content, Array<Launchpad>::class.java)
    }
}
