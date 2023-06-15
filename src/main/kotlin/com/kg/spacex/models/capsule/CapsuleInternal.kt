package com.kg.spacex.models.capsule

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class CapsuleInternal (
    val id: String,
    val serial: String,
    val type: String,
    var status: String,
    var last_update: String?,
    var water_landings: Long? = 0,
    var land_landings: Long? = 0,

    @SerializedName(value = "launches")
    val launchIds: List<String>

    ) {
    class Deserializer : ResponseDeserializable<CapsuleInternal> {
        override fun deserialize(content: String): CapsuleInternal =
            Gson().fromJson(content, CapsuleInternal::class.java)
    }
    class ArrayDeserializer : ResponseDeserializable<Array<CapsuleInternal>> {
        override fun deserialize(content: String): Array<CapsuleInternal> =
            Gson().fromJson(content, Array<CapsuleInternal>::class.java)
    }
}
