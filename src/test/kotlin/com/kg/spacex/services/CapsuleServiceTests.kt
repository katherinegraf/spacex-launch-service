package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchCapsuleRepository
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
    @Autowired
    private lateinit var launchCapsuleRepo: LaunchCapsuleRepository

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val capsuleServiceMock = CapsuleService(apiServiceMock)
    private val capsuleRepoMock = mockk<CapsuleRepository>()

    @Test
    fun fetchOneCapsuleSuccess() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { capsuleInternalMock }
        val result = capsuleServiceMock.fetchOneCapsule(capsuleInternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result)
    }

    @Test
    fun fetchOneCapsuleFailures() {
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
    fun makeAPICallSuccess() {
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
    fun makeAPICallFailures() {
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
    fun saveAndUpdateCapsulesTest() {
        val capsules = listOf(capsuleInternalMock)
        capsuleService.updateOrSaveCapsules(capsules)
        val firstResult = capsuleRepo.findByIdOrNull(capsuleInternalMock.id)
        var expectedLastUpdate = capsuleInternalMock.last_update
        assert(firstResult != null)
        assert(firstResult?.last_update == expectedLastUpdate)
        capsuleService.updateOrSaveCapsules(listOf(editedCapsuleInternalMock))
        expectedLastUpdate = editedCapsuleInternalMock.last_update
        val secondResult = capsuleRepo.findByIdOrNull(editedCapsuleInternalMock.id)
        assert(secondResult?.last_update == expectedLastUpdate)
    }

    @Test
    fun getCapsulesForLaunchFailures_InvalidLaunchID_CapsuleNotExists() {
        // First premise: invalid launch ID
        val invalidLaunchId = "abc"
        val firstResult = capsuleService.getCapsulesForLaunch(invalidLaunchId)
        assert(firstResult == null)
        // Second premise: launchCapsule record exists, but Capsule record not exists
        capsuleService.updateLaunchCapsuleJoinTable(capsuleInternalMock)
        val launchId = capsuleInternalMock.launchIds[0]
        val secondResult = capsuleService.getCapsulesForLaunch(launchId)
        assert(secondResult == null)
    }

}
