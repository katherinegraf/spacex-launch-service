package com.kg.spacex.controllers

import com.kg.spacex.mocks.launchExternalMock
import com.kg.spacex.mocks.launchInternalMock
import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.Payload
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.services.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest
class SpaceXControllerTests {

    @Autowired
    private lateinit var spaceXController: SpaceXController

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val capsuleService = CapsuleService(apiServiceMock)
    private val launchpadService = LaunchpadService(apiServiceMock)
    private val payloadService = PayloadService(apiServiceMock)
    private val launchService = LaunchService(
        capsuleService, launchpadService, payloadService, apiServiceMock
    )
    private val spaceXControllerMock = SpaceXController(
        apiServiceMock, capsuleService, launchService, launchpadService, payloadService
    )

    @Test
    fun indexTestSuccess() {
        val result = spaceXController.index()
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is List<LaunchExternal>)
    }

    @Test
    fun indexTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val result = spaceXControllerMock.index()
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getOneLaunchExternalTestSuccess() {
        val launchId = launchExternalMock.id
        val result = spaceXController.getOneLaunchExternal(launchId)
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is LaunchExternal)
    }

    @Test
    fun getOneLaunchExternalTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val launchId = launchExternalMock.id
        val firstResult = spaceXControllerMock.getOneLaunchExternal(launchId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult.statusCode == HttpStatus.NOT_FOUND)
        val invalidId = "abc"
        val secondResult = spaceXController.getOneLaunchExternal(invalidId)
        assert(secondResult.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getOneLaunchInternalTestSuccess() {
        val launchId = launchInternalMock.id
        val result = spaceXController.getOneLaunchInternal(launchId)
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is LaunchInternal)
    }

    @Test
    fun getOneLaunchInternalTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val launchId = launchInternalMock.id
        val firstResult = spaceXControllerMock.getOneLaunchInternal(launchId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult.statusCode == HttpStatus.NOT_FOUND)
        val invalidId = "abc"
        val secondResult = spaceXController.getOneLaunchInternal(invalidId)
        assert(secondResult.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getOneCapsuleTestSuccess() {
        val capsuleId = launchInternalMock.capsules[0]
        val result = spaceXController.getOneCapsule(capsuleId)
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is Capsule)
    }

    @Test
    fun getOneCapsuleTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val capsuleId = launchInternalMock.capsules[0]
        val firstResult = spaceXControllerMock.getOneCapsule(capsuleId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult.statusCode == HttpStatus.NOT_FOUND)
        val invalidId = "abc"
        val secondResult = spaceXController.getOneCapsule(invalidId)
        assert(secondResult.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getOnePayloadTestSuccess() {
        val payloadId = launchInternalMock.payloads[0]
        val result = spaceXController.getOnePayload(payloadId)
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is Payload)
    }

    @Test
    fun getOnePayloadTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val payloadId = launchInternalMock.payloads[0]
        val firstResult = spaceXControllerMock.getOnePayload(payloadId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult.statusCode == HttpStatus.NOT_FOUND)
        val invalidId = "abc"
        val secondResult = spaceXController.getOnePayload(invalidId)
        assert(secondResult.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getOneLaunchpadTestSuccess() {
        val launchpadId = launchInternalMock.launchpad
        val result = spaceXController.getOneLaunchpad(launchpadId)
        assert(result.statusCode == HttpStatus.OK)
        assert(result.body is Launchpad)
    }

    @Test
    fun getOneLaunchpadTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val launchpadId = launchInternalMock.launchpad
        val firstResult = spaceXControllerMock.getOneLaunchpad(launchpadId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult.statusCode == HttpStatus.NOT_FOUND)
        val invalidId = "abc"
        val secondResult = spaceXController.getOneLaunchpad(invalidId)
        assert(secondResult.statusCode == HttpStatus.NOT_FOUND)
    }
}
