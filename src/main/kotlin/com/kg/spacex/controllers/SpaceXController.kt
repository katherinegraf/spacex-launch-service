package com.kg.spacex.controllers

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

// TODO add 'updated_on' date field somewhere
//  have controller endpoints compare that field to today
//  and only fetch new data if is outside allowed range (ie: 7 days)
// TODO create a function that triggers updating all table data
// TODO remove return object functionality from fetch calls;
//  change controller functions to get objects from new read db functions
// TODO new flow is:
//  - controller calls check date method to compare today to updated_on field
//  - if within range, read from db
//  - if outside range, trigger fetches to update db
//      - then read from db

// TODO update tests

/**
 * Connects to [launchService], [launchpadService], [payloadService], and [capsuleService]
 * to access the calling methods in those service classes.
 *
 * @property spaceXAPIService exists for testing, so that an instance of SpaceXController
 * can be generated using a mocked API service.
 */
@RestController
class SpaceXController (
    private val spaceXAPIService: SpaceXAPIService,
    private val capsuleService: CapsuleService,
    private val launchService: LaunchService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService
) {

    @GetMapping("spacex-launches/{launchId}")
    fun getOneLaunchExternal(
        @PathVariable("launchId") launchId: String
    ): ResponseEntity<LaunchExternal> {
        val launchExternal = launchService.fetchAndSaveOneLaunch(launchId)
        return if (launchExternal == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchExternal, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/new/{launchId}")
    fun getOneLaunchFromDb(
        @PathVariable("launchId") launchId: String
    ): ResponseEntity<LaunchExternal> {
        val launchExternal = launchService.buildLaunchExternalFromDbById(launchId)
        return if (launchExternal == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchExternal, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/")
    fun getAllLaunches(): ResponseEntity<List<LaunchExternal>> {
        val launches = launchService.fetchAndSaveAllLaunches()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/new/")
    fun getAllLaunchesFromDb(): ResponseEntity<List<LaunchExternal>>? {
        val launches = launchService.getAllLaunchesFromDb()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/capsules/{id}")
    fun getOneCapsule(
        @PathVariable("id") capsuleId: String
    ): ResponseEntity<Capsule> {
        val capsule = capsuleService.fetchOneCapsule(capsuleId)
        return if (capsule == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsule, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/new/capsules/{id}")
    fun getOneCapsuleFromDb(
        @PathVariable("id") capsuleId: String
    ): ResponseEntity<List<Capsule>> {
        val capsule = capsuleService.getCapsulesById(listOf(capsuleId))
        return if (capsule == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsule, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/capsules/")
    fun getAllCapsules(): ResponseEntity<List<Capsule>> {
        val capsules = capsuleService.fetchAndSaveAllCapsules()
        return if (capsules == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsules, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/payloads/{id}")
    fun getOnePayload(
        @PathVariable("id") payloadId: String
    ): ResponseEntity<PayloadExternal> {
        val payload = payloadService.fetchOnePayload(payloadId)
        return if (payload == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(payload, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/payloads/")
    fun getAllPayloads(): ResponseEntity<List<PayloadExternal>> {
        val payloads = payloadService.fetchAndSaveAllPayloads()
        return if (payloads == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(payloads, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/launchpads/{id}")
    fun getOneLaunchpad(
        @PathVariable("id") launchpadId: String
    ): ResponseEntity<Launchpad> {
        val launchpad = launchpadService.fetchOneLaunchpad(launchpadId)
        return if (launchpad == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchpad, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/launchpads/")
    fun getAllLaunchpads(
    ): ResponseEntity<List<Launchpad>> {
        val launchpads = launchpadService.fetchAndSaveAllLaunchpads()
        return if (launchpads == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchpads, HttpStatus.OK)
        }
    }
}
