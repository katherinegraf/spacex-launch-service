package com.kg.spacex.services

import com.kg.spacex.mocks.launchInternalMock
import com.kg.spacex.mocks.launchpadMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LaunchpadServiceTests {

    @Autowired
    private lateinit var launchpadService: LaunchpadService

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val launchpadServiceMock = LaunchpadService(apiServiceMock)

    @Test
    fun fetchLaunchpadTestSuccess() {
        val launchpadId = launchInternalMock.launchpad
        val expectedFullName = launchpadMock.full_name
        val result = launchpadService.fetchLaunchpad(launchpadId)
        assert(result?.full_name == expectedFullName)
    }

    @Test
    fun fetchLaunchpadTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val launchpadId = launchInternalMock.launchpad
        val firstResult = launchpadServiceMock.fetchLaunchpad(launchpadId)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult == null)
        val invalidId = "abc"
        val secondResult = launchpadService.fetchLaunchpad(invalidId)
        assert(secondResult == null)
    }
}
