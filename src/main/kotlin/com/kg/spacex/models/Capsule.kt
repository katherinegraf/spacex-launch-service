package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

class Capsule (
    val serial: String,
    val id: String,
    val status: String,
    val last_update: String,
    val water_landings: Long? = 0,
    val land_landings: Long? = 0,
) {
    class Deserializer : ResponseDeserializable<Capsule> {
        override fun deserialize(content: String): Capsule =
            Gson().fromJson(content, Capsule::class.java)
    }
}
