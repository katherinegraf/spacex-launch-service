package com.kg.spacex.models.launch

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kg.spacex.models.launch.failure.FailureInternal

class LaunchInternal (
    val name: String,
    val details: String?,
    val date_utc: String,
    val success: Boolean,
    val failures: List<FailureInternal>,
    val id: String,

    @SerializedName(value = "launchpad")
    val launchpadId: String,

    @SerializedName(value = "payloads")
    val payloadIds: List<String>,

    @SerializedName(value = "capsules")
    val capsuleIds: List<String>
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
