package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.PayloadRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayloadServiceTests {

    @Autowired
    private lateinit var payloadService: PayloadService
    @Autowired
    private lateinit var payloadRepo: PayloadRepository

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val payloadServiceMock = PayloadService(apiServiceMock)

    @BeforeAll
    fun setup(
        @Autowired launchService: LaunchService,
        @Autowired launchpadService: LaunchpadService
    ) {
        launchpadService.updateOrSaveLaunchpads(listOf(launchpadMock))
        launchService.updateOrSaveLaunches(listOf(launchInternalMock))
    }

    @AfterAll
    fun tearDown(
        @Autowired launchRepo: LaunchRepository,
        @Autowired launchpadRepo: LaunchRepository
    ) {
        payloadRepo.deleteAll()
        launchRepo.deleteAll()
        launchpadRepo.deleteAll()
    }

    @Test
    fun fetchOnePayload_Success() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { payloadInternalMock }
        val result = payloadServiceMock.fetchOnePayload(payloadInternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result)
    }

    @Test
    fun fetchOnePayload_Failure() {
        /*
        1. Given invalid id, expect fetch to return null
        2. Given API call behavior mocked to return null, expect fetch to return null
         */
        val payloadId = "abc"
        val firstResult = payloadService.fetchOnePayload(payloadId)
        assert(!firstResult)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = payloadServiceMock.fetchOnePayload(payloadInternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(!secondResult)
    }

    @Test
    fun makeAPICall_Success() {
        val payloadId = payloadInternalMock.id
        val expectedName = payloadInternalMock.name
        val firstResult = payloadService.makeAPICall(payloadId) as PayloadInternal?
        assert(firstResult?.name == expectedName)
        val secondResult = payloadService.makeAPICall(null) as Array<*>
        val secondResultIds = mutableListOf<String>()
        secondResult.forEach { result ->
            result as PayloadInternal
            secondResultIds.add(result.id)
        }
        assert(payloadId in secondResultIds)
    }

    @Test
    fun makeAPICall_Failure() {
        /*
        1. Given invalid id, expect makeAPICall to return null
        2. Given API call behavior mocked to return null, expect makeAPICall to return null
        */
        val payloadId = "abc"
        val firstResult = payloadService.makeAPICall(payloadId)
        assert(firstResult == null)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = payloadServiceMock.makeAPICall(null)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(secondResult == null)
    }

    @Test
    fun saveAndUpdatePayload_Success() {
        /*
        1. Given a payload whose associated launch (and that launch's associated launchpad)
            exist in their respective tables, expect that payload will be saved successfully.
        2. Given a payload with updated name attribute, expect that payload record will
            be updated successfully.
         */
        val payloads = listOf(payloadInternalMock)
        payloadService.updateOrSavePayloads(payloads)
        val firstResult = payloadRepo.findByIdOrNull(payloadInternalMock.id)
        var expectedName = payloadInternalMock.name
        assert(firstResult != null)
        assert(firstResult?.name == expectedName)
        payloadService.updateOrSavePayloads(listOf(payloadInternalMockEdited))
        expectedName = payloadInternalMockEdited.name
        val secondResult = payloadRepo.findByIdOrNull(payloadInternalMockEdited.id)
        assert(secondResult?.name == expectedName)
    }

    @Test
    fun savePayload_Failure() {
        /*
        Expect that given a launch ID that doesn't exist in launches table,
        updateOrSavePayloads will not save payloadInvalidLaunchMock,
        therefore findByIdOrNull will return null.
         */
        val payloads = listOf(payloadMockInvalidLaunchId)
        payloadService.updateOrSavePayloads(payloads)
        val foundPayload = payloadRepo.findByIdOrNull(payloads[0].id)
        assert(foundPayload == null)
    }

    @Test
    fun getPayloadsByLaunchId_Success() {
        payloadService.updateOrSavePayloads(listOf(payloadInternalMockEdited))
        val launchId = launchExternalMock.id
        val expectedName = payloadExternalMock.name
        val result = payloadService.getPayloadsByLaunchId(launchId)
        assert(result[0].name == expectedName)
    }

    @Test
    fun getPayloadsByLaunchId_Failure() {
        /*
        Given a launch ID that doesn't exist in launches table,
        expect there won't be a record in payloads table with that launch ID,
        therefore expect getPayloadsByLaunchID will return an empty list.
         */
        val launchId = payloadMockInvalidLaunchId.launchId
        val result = payloadService.getPayloadsByLaunchId(launchId)
        assert(result.isEmpty())
    }
}
