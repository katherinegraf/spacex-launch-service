package com.kg.spacex.services

import com.kg.spacex.mocks.launchInternalMock
import com.kg.spacex.models.Capsule
import com.kg.spacex.utils.SPACEX_API_CAPSULES_URL
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpaceXAPIServiceTests {

    @Autowired
    private lateinit var spaceXAPIService: SpaceXAPIService

    private val apiServiceMock = mockk<SpaceXAPIService>()

    @Test
    fun handleAPICallTestSuccess() {
        val capsuleId = launchInternalMock.capsules[0]
        val result = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL.plus(capsuleId),
            deserializer = Capsule.Deserializer()
        )
        assert(result != null)
        assert(result is Capsule)
    }

    @Test
    fun fetchCapsulesTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val capsuleId = launchInternalMock.capsules[0]
        val firstResult = apiServiceMock.handleAPICall(
            url = SPACEX_API_CAPSULES_URL.plus(capsuleId),
            deserializer = Capsule.Deserializer()
        )
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult == null)
        val invalidId = "abc"
        val secondResult = spaceXAPIService.handleAPICall(
            url = SPACEX_API_CAPSULES_URL.plus(invalidId),
            deserializer = Capsule.Deserializer()
        )
        assert(secondResult == null)
    }
}
