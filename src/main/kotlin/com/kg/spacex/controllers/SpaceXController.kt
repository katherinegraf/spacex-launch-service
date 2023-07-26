package com.kg.spacex.controllers

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.services.*
import com.kg.spacex.utils.ErrorMessage
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import java.util.logging.Logger

/**
 * @property spaceXAPIService exists for testing, so that an instance of SpaceXController
 * can be generated using a mocked API service.
 */
@RestController
@RequestMapping("/spacex-launches")
class SpaceXController (
    private val spaceXAPIService: SpaceXAPIService,
    private val capsuleService: CapsuleService,
    private val launchService: LaunchService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService
) {

    val logger: Logger = Logger.getLogger("logger")

    // TODO can this be removed since I'm using try/catch blocks?
    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(e: HttpClientErrorException.NotFound): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @GetMapping("/")
    fun index(): Any {
        return try {
            launchService.getAllLaunchExternalsFromDb()
        } catch (exception: ResourceNotFoundException) {
            val errorMessage = ErrorMessage(status = 404, "Unable to retrieve launches from database")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
//        val dataRefreshNeeded = launchService.isDataRefreshNeeded()
//        if (dataRefreshNeeded) {
//            launchService.refreshAllData() ?: return ResponseEntity(HttpStatus.NOT_FOUND)
//        }
//        val launches = launchService.getAllLaunchExternalsFromDb()
//        return if (launches == null) {
//            ResponseEntity(HttpStatus.NOT_FOUND)
//        } else {
//            ResponseEntity(launches, HttpStatus.OK)
//        }
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
//        val launch = launchService.buildLaunchExternalById(launchId)
//        return if (launch == null) {
//            ResponseEntity(HttpStatus.NOT_FOUND)
//        } else {
//            ResponseEntity(launch, HttpStatus.OK)
//        }
    }

    @GetMapping("/capsules/{id}")
    fun getCapsuleFromDb(
        @PathVariable("id") id: String
    ): Any {
        return try {
            capsuleService.getByIds(listOf(id))[0]
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

    @GetMapping("launchpad/test/{id}")
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

    @GetMapping("launchpad/test")
    fun getLaunchpadsFromAPI(): Any {
        return try {
            launchpadService.fetchAll()
        } catch (exception: ResourceUnavailableException) {
            val errorMessage = ErrorMessage(status = 404,"API resource unavailable")
            ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("capsule/new")
    fun add(
        @RequestBody capsule: CapsuleInternal
    ) {
        capsuleService.saveOrUpdate(listOf(capsule))
    }

    @GetMapping("capsule/getByLaunch/{id}")
    fun getCapsuleByLaunchId(
        @PathVariable id: String
    ): List<CapsuleExternal> {
        return capsuleService.getCapsulesForLaunch(id)
    }
}
