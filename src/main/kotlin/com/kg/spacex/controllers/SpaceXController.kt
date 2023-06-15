package com.kg.spacex.controllers

import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

// TODO add 'updated_on' date field somewhere
//  have controller endpoints compare that field to today
//  and only fetch new data if is outside allowed range (ie: 7 days)
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

    @GetMapping("spacex-launches/refresh")
    fun refreshData(): ResponseEntity<List<LaunchExternal>> {
        launchService.fetchAllData()
        val launches = launchService.getAllLaunchesFromDb()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
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

    @GetMapping("spacex-launches/new/")
    fun getAllLaunchesFromDb(): ResponseEntity<List<LaunchExternal>>? {
        val launches = launchService.getAllLaunchesFromDb()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
        }
    }

    @GetMapping("spacex-launches/new/capsules/{id}")
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
