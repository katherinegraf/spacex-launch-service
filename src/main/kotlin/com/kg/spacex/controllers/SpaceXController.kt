package com.kg.spacex.controllers

import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
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

    @GetMapping("spacex-launches/")
    fun index(): ResponseEntity<List<LaunchExternal>> {
        val dataRefreshNeeded = launchService.isDataRefreshNeeded()
        if (dataRefreshNeeded) {
            launchService.fetchAllData() ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        val launches = launchService.getAllLaunchesFromDb()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/{launchId}")
    fun getOneLaunchFromDb(
        @PathVariable("launchId") launchId: String
    ): ResponseEntity<LaunchExternal> {
        val launch = launchService.getLaunchExternalFromDbById(launchId)
        return if (launch == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launch, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/capsules/{id}")
    fun getOneCapsuleFromDb(
        @PathVariable("id") capsuleId: String
    ): ResponseEntity<CapsuleExternal> {
        val capsule = capsuleService.getCapsulesById(listOf(capsuleId))
        return if (capsule == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsule[0], HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/payloads/{id}")
    fun getOnePayloadFromDb(
        @PathVariable("id") payloadId: String
    ): ResponseEntity<PayloadExternal> {
        val payload = payloadService.getPayloadByID(payloadId)
        return if (payload == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(payload, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/launchpad/{id}")
    fun getOneLaunchpadFromDb(
        @PathVariable("id") launchpadId: String
    ): ResponseEntity<Launchpad> {
        val launchpad = launchpadService.getLaunchpadById(launchpadId)
        return if (launchpad == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchpad, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/launchpad/test/{id}")
    fun fetchInvalidLaunchpad(
        @PathVariable("id") launchpadId: String
    ): Any? {
        val result = launchpadService.fetchOneLaunchpad(launchpadId)
        return if (result == null) {
            ResponseEntity(null, HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity.ok(null)
        }
    }

}
