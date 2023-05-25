package com.kg.spacex.services

import com.kg.spacex.mocks.launchInternalMock
import com.kg.spacex.mocks.capsulesMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CapsuleServiceTests {

    @Autowired
    private lateinit var capsuleService: CapsuleService

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val capsuleServiceMock = CapsuleService(apiServiceMock)

    @Test
    fun fetchCapsulesTestSuccess() {
        val capsuleIdList = launchInternalMock.capsules
        val expectedSerial = capsulesMock[0].serial
        val result = capsuleService.fetchCapsules(capsuleIdList)
        assert(result != null)
        assert(result?.get(0)?.serial == expectedSerial)
    }

    @Test
    fun fetchCapsulesTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val capsuleIds = launchInternalMock.capsules
        val firstResult = capsuleServiceMock.fetchCapsules(capsuleIds)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult == null)
        val invalidId = listOf("abc")
        val secondResult = capsuleService.fetchCapsules(invalidId)
        assert(secondResult == null)
    }
}
