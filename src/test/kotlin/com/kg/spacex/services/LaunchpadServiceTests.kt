package com.kg.spacex.services

import com.kg.spacex.mocks.launchpadMock
import com.kg.spacex.mocks.editedLaunchpadMock
import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
@AutoConfigureTestDatabase
class LaunchpadServiceTests {

    @Autowired
    private lateinit var launchpadService: LaunchpadService

    @Autowired
    private lateinit var launchpadRepo: LaunchpadRepository

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val launchpadServiceMock = LaunchpadService(apiServiceMock)

    @Test
    fun fetchOneLaunchpadSuccess() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { launchpadMock }
        val result = launchpadServiceMock.fetchOneLaunchpad(launchpadMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result)
    }

    @Test
    fun fetchOneLaunchpadFailures() {
        val id = "abc"
        val firstResult = launchpadService.fetchOneLaunchpad(id)
        assert(!firstResult)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = launchpadServiceMock.fetchOneLaunchpad(launchpadMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(!secondResult)
    }

    @Test
    fun makeAPICallSuccess() {
        val launchpadId = launchpadMock.id
        val expectedFullName = launchpadMock.full_name
        val firstResult = launchpadService.makeAPICall(launchpadId) as Launchpad?
        assert(firstResult?.full_name == expectedFullName)
        val secondResult = launchpadService.makeAPICall(null) as Array<*>?
        val secondResultIds = mutableListOf<String>()
        secondResult?.forEach { result ->
            result as Launchpad
            secondResultIds.add(result.id)
        }
        assert(launchpadId in secondResultIds)
    }

    @Test
    fun makeAPICallFailures() {
        val launchpadId = "abc"
        val firstResult = launchpadService.makeAPICall(launchpadId)
        assert(firstResult == null)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = launchpadServiceMock.makeAPICall(null)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(secondResult == null)
    }

    @Test
    fun saveAndUpdateLaunchpadsTest() {
        val launchpads = listOf(launchpadMock)
        launchpadService.updateOrSaveLaunchpads(launchpads)
        val firstResult = launchpadRepo.findByIdOrNull(launchpadMock.id)
        val expectedDetails = launchpadMock.details
        assert(firstResult != null)
        assert(firstResult?.details == expectedDetails)
        launchpadService.updateOrSaveLaunchpads(listOf(editedLaunchpadMock))
        val secondResult = launchpadRepo.findByIdOrNull(editedLaunchpadMock.id)
        assert(secondResult?.details == editedLaunchpadMock.details)
    }
}
