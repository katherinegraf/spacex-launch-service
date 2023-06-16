package com.kg.spacex.controllers

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

// TODO update tests

// TODO add endpoints for single payload, launchpad
//  should they instead handle requests for one or all?

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

    @GetMapping("spacex-launches/")
    fun index(): ResponseEntity<List<LaunchExternal>> {
        val dataRefreshNeeded = launchService.isDataRefreshNeeded()
        if (dataRefreshNeeded) {
            launchService.fetchAllData()
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
        val launchExternal = launchService.buildLaunchExternalFromDbById(launchId)
        return if (launchExternal == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchExternal, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/capsules/{id}")
    fun getOneCapsuleFromDb(
        @PathVariable("id") capsuleId: String
    ): ResponseEntity<List<CapsuleExternal>> {
        val capsule = capsuleService.getCapsulesById(listOf(capsuleId))
        return if (capsule == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsule, HttpStatus.OK)
        }
    }

}
