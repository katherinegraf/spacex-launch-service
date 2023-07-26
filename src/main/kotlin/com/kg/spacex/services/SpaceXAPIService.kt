package com.kg.spacex.services

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.kg.spacex.utils.ResourceUnavailableException
import org.springframework.stereotype.Service

/**
 * Handles all calls to the SpaceX API.
 */
@Service
class SpaceXAPIService {

    /**
     * Makes call to the SpaceX API based on the given parameters.
     *
     * If API call fails, a custom exception is thrown.
     *
     * @param url is the API endpoint to hit.
     * @param deserializer is which deserializer to use.
     * @return if call is successful, returns either the object/collection retrieved or null.
     */
    fun handleAPICall(
        url: String,
        deserializer: ResponseDeserializable<Any>,
    ): Any? {
        val (_, _, result) = url
            .httpGet()
            .responseObject(deserializer)

        return when (result) {
            is Result.Failure -> {
                throw ResourceUnavailableException()
            }
            is Result.Success -> {
                val (responseObject) = result
                responseObject
            }
        }
    }
}
