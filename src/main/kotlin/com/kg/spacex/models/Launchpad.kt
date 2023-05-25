package com.kg.spacex.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

class Launchpad (
    val full_name: String,
    val locality: String,
    val region: String,
    val status: String,
    val details: String,
    val launch_attempts: Long,
    val launch_successes: Long
) {
    class Deserializer : ResponseDeserializable<Launchpad> {
        override fun deserialize(content: String): Launchpad =
            Gson().fromJson(content, Launchpad::class.java)
    }
}
