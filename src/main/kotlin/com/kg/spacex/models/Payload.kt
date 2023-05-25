package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

class Payload (
    val name: String,
    val type: String,
    val regime: String,
    val customers: List<String>,
    val nationalities: List<String>,
    val manufacturers: List<String>,
    val mass_kg: Float,
    val mass_lbs: Float
) {
    class Deserializer : ResponseDeserializable<Payload> {
        override fun deserialize(content: String): Payload =
            Gson().fromJson(content, Payload::class.java)
    }
}
