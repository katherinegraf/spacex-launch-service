package com.kg.spacex.models.launch

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

class LaunchInternal (
    val name: String,
    val details: String?,
    val date_utc: String,
    val success: Boolean,
    val failures: List<Map<String, String>>,
    val id: String,
    val launchpad: String,
    val payloads: List<String>,
    val capsules: List<String>
) {
    class Deserializer : ResponseDeserializable<LaunchInternal> {
        override fun deserialize(content: String): LaunchInternal =
            Gson().fromJson(content, LaunchInternal::class.java)
    }
    class ArrayDeserializer : ResponseDeserializable<Array<LaunchInternal>> {
        override fun deserialize(content: String): Array<LaunchInternal> =
            Gson().fromJson(content, Array<LaunchInternal>::class.java)
    }

}
