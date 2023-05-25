package com.kg.spacex.services

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Handles all calls to the SpaceX API.
 */
@Service
class SpaceXAPIService {

    val logger = Logger.getLogger("logger")

    /**
     * Fetches the requested object from the SpaceX API based on the given parameters.
     *
     * If API call fails, a warning is logged detailing the exception experienced and the function returns null.
     *
     * @param url is the API endpoint to hit.
     * @param deserializer is which deserializer to use to deserialize the object returned from the API.
     * @return the object retrieved, or null if the API call fails or returns null.
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
                val exception = result.getException()
                logger.warning("Exception is $exception")
                null
            }
            is Result.Success -> {
                val (apiResult) = result
                (apiResult)
            }
        }
    }
}
