package com.kg.spacex.models.payload

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class PayloadInternal (
    val id: String,
    val name: String,
    val type: String,
    val regime: String?,

    @SerializedName(value = "launch")
    val launchId: String,

    val customers: List<String>,
    val nationalities: List<String>,
    val manufacturers: List<String>,
    val mass_kg: Float,
    val mass_lbs: Float
) {
    class Deserializer : ResponseDeserializable<PayloadInternal> {
        override fun deserialize(content: String): PayloadInternal =
            Gson().fromJson(content, PayloadInternal::class.java)
    }
    class ArrayDeserializer : ResponseDeserializable<Array<PayloadInternal>> {
        override fun deserialize(content: String): Array<PayloadInternal> =
            Gson().fromJson(content, Array<PayloadInternal>::class.java)
    }
}
