package com.kg.spacex.controllers

import com.kg.spacex.services.*
import com.kg.spacex.utils.ErrorMessage
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/spacex-launches")
class SpaceXController (
    private val spaceXAPIService: SpaceXAPIService,
    private val capsuleService: CapsuleService,
    private val launchService: LaunchService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService
) {

    @GetMapping("/")
    fun index(): Any {
        return try {
            launchService.getAll()
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Unable to retrieve launch data from database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/launches/{id}")
    fun getLaunchFromDb(
        @PathVariable id: String
    ): Any {
        return try {
            launchService.buildLaunchExternalById(id)
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Could not find that id in database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/capsules/{id}")
    fun getCapsuleFromDb(
        @PathVariable("id") id: String
    ): Any {
        return try {
            capsuleService.getById(id)
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Could not find that id in database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/payloads/{id}")
    fun getPayloadFromDb(
        @PathVariable("id") payloadId: String
    ): Any {
        return try {
            payloadService.getById(payloadId)
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Could not find that id in database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/launchpads/{id}")
    fun getLaunchpadFromDb(
        @PathVariable("id") launchpadId: String
    ): Any {
        return try {
            launchpadService.getById(launchpadId)
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Could not find that id in database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("launches/api/{id}")
    fun getLaunchFromAPI(
        @PathVariable("id") id: String
    ): Any {
        return try {
            launchService.fetchOne(id)
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }
    @GetMapping("launches/api")
    fun getLaunchesFromAPI(): Any {
        return try {
            launchService.fetchAll()
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("capsules/api/{id}")
    fun getCapsuleFromAPI(
        @PathVariable("id") id: String
    ): Any {
        return try {
            capsuleService.fetchOne(id)
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("capsules/api")
    fun getCapsulesFromAPI(): Any {
        return try {
            capsuleService.fetchAll()
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("payloads/api/{id}")
    fun getPayloadFromAPI(
        @PathVariable("id") id: String
    ): Any {
        return try {
            payloadService.fetchOne(id)
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("payloads/api")
    fun getPayloadsFromAPI(): Any {
        return try {
            payloadService.fetchAll()
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("launchpads/api/{id}")
    fun getLaunchpadFromAPI(
        @PathVariable("id") id: String
    ): Any {
        return try {
            launchpadService.fetchOne(id)
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("launchpads/api")
    fun getLaunchpadsFromAPI(): Any {
        return try {
            launchpadService.fetchAll()
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }
}
