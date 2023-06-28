package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.repos.CapsuleRepository
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
class CapsuleServiceTests {

    @Autowired
    private lateinit var capsuleService: CapsuleService
    @Autowired
    private lateinit var capsuleRepo: CapsuleRepository

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val capsuleServiceMock = CapsuleService(apiServiceMock)

    @Test
    fun fetchOneCapsule_Success() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { capsuleInternalMock }
        val result = capsuleServiceMock.fetchOneCapsule(capsuleInternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result)
    }

    @Test
    fun fetchOneCapsule_Failure() {
        /*
        1. Given invalid id, expect fetch to return null
        2. Given API call behavior mocked to return null, expect fetch to return null
         */
        val capsuleId = "abc"
        val firstResult = capsuleService.fetchOneCapsule(capsuleId)
        assert(!firstResult)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = capsuleServiceMock.fetchOneCapsule(capsuleInternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(!secondResult)
    }

    @Test
    fun makeAPICall_Success() {
        val capsuleId = capsuleInternalMock.id
        val expectedSerial = capsuleInternalMock.serial
        val firstResult = capsuleService.makeAPICall(capsuleId) as CapsuleInternal?
        assert(firstResult?.serial == expectedSerial)
        val secondResult = capsuleService.makeAPICall(null) as Array<*>
        val secondResultIds = mutableListOf<String>()
        secondResult.forEach { result ->
            result as CapsuleInternal
            secondResultIds.add(result.id)
        }
        assert(capsuleId in secondResultIds)
    }

    @Test
    fun makeAPICall_Failure() {
        /*
        1. Given invalid id, expect makeAPICall to return null
        2. Given API call behavior mocked to return null, expect makeAPICall to return null
        */
        val capsuleId = "abc"
        val firstResult = capsuleService.makeAPICall(capsuleId)
        assert(firstResult == null)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = capsuleServiceMock.makeAPICall(null)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(secondResult == null)
    }

    @Test
    fun saveAndUpdateCapsules_Success() {
        val capsules = listOf(capsuleInternalMock)
        capsuleService.updateOrSaveCapsules(capsules)
        val firstResult = capsuleRepo.findByIdOrNull(capsuleInternalMock.id)
        var expectedLastUpdate = capsuleInternalMock.last_update
        assert(firstResult != null)
        assert(firstResult?.last_update == expectedLastUpdate)
        capsuleService.updateOrSaveCapsules(listOf(capsuleInternalMockEdited))
        expectedLastUpdate = capsuleInternalMockEdited.last_update
        val secondResult = capsuleRepo.findByIdOrNull(capsuleInternalMockEdited.id)
        assert(secondResult?.last_update == expectedLastUpdate)
    }

    @Test
    fun getCapsulesForLaunch_Failure() {
        /*
        Given an invalid launchId, expect that no matching records exist in
        launch_capsule_details table, therefore no matching capsule Ids can be found
        to look for in capsules table, so expect getCapsulesForLaunch to return null.
         */
        val invalidLaunchId = "abc"
        val firstResult = capsuleService.getCapsulesForLaunch(invalidLaunchId)
        assert(firstResult.isEmpty())
    }
}
