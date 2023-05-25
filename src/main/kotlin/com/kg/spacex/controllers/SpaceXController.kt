package com.kg.spacex.controllers

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.Payload
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.services.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

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

    /**
     * Gets all launches from SpaceX with details about payloads, launchpad, and capsules.
     *
     * @return either 200/OK and list of LaunchExternals or 404/Not Found.
     */
    @GetMapping("spacex-launches/index")
    fun index(): ResponseEntity<List<LaunchExternal>> {
        val launches = launchService.getListOfLaunchExternals()
        return if (launches == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launches, HttpStatus.OK)
        }
    }

    /**
     * Gets one launch from SpaceX with details about payloads, launchpad, and capsules.
     *
     * @param launchId is id for a launch.
     * @return either 200/OK and one LaunchExternal object or 404/Not Found.
     */
    @GetMapping("spacex-launches/{launchId}")
    fun getOneLaunchExternal(
        @PathVariable("launchId") launchId: String
    ): ResponseEntity<LaunchExternal> {
        val launchExternal = launchService.getOneLaunchExternal(launchId)
        return if (launchExternal == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchExternal, HttpStatus.OK)
        }
    }

    /**
     * Gets one launch from SpaceX with ids for payloads, launchpad, and capsules.
     *
     * @param launchId is id for a launch.
     * @return either 200/OK and one LaunchInternal object or 404/Not Found.
     */
    @GetMapping("spacex-launches/launch-internal/{id}")
    fun getOneLaunchInternal(
        @PathVariable("id") launchId: String
    ): ResponseEntity<LaunchInternal> {
        val launchInternal = launchService.fetchOneLaunchFromSpaceX(launchId)
        return if (launchInternal == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchInternal, HttpStatus.OK)
        }
    }

    /**
     * Gets one capsule from SpaceX.
     *
     * @param capsuleId is id for a capsule.
     * @return either 200/OK and one Capsule object or 404/Not Found.
     */
    @GetMapping("spacex-launches/capsule/{id}")
    fun getOneCapsule(
        @PathVariable("id") capsuleId: String
    ): ResponseEntity<Capsule> {
        val capsuleList = capsuleService.fetchCapsules(listOf(capsuleId))
        return if (capsuleList == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(capsuleList[0], HttpStatus.OK)
        }
    }

    /**
     * Gets one payload from SpaceX.
     *
     * @param payloadId is id for a payload.
     * @return either 200/OK and one Payload object or 404/Not Found.
     */
    @GetMapping("spacex-launches/payload/{id}")
    fun getOnePayload(
        @PathVariable("id") payloadId: String
    ): ResponseEntity<Payload> {
        val payloadList = payloadService.fetchPayloads(listOf(payloadId))
        return if (payloadList == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(payloadList[0], HttpStatus.OK)
        }
    }

    /**
     * Gets one launchpad from SpaceX.
     *
     * @param launchpadId is id for a launchpad.
     * @return either 200/OK and one Launchpad object or 404/Not Found.
     */
    @GetMapping("spacex-launches/launchpad/{id}")
    fun getOneLaunchpad(
        @PathVariable("id") launchpadId: String
    ): ResponseEntity<Launchpad> {
        val launchpad = launchpadService.fetchLaunchpad(launchpadId)
        return if (launchpad == null) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(launchpad, HttpStatus.OK)
        }
    }
}
