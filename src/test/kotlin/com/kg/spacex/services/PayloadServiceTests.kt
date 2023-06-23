package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.PayloadRepository
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
class PayloadServiceTests {

    @Autowired
    private lateinit var payloadService: PayloadService

    @Autowired
    private lateinit var payloadRepo: PayloadRepository

    @Autowired
    private lateinit var launchService: LaunchService

    @Autowired
    private lateinit var launchpadService: LaunchpadService

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val payloadServiceMock = PayloadService(apiServiceMock)
    private val payloadRepoMock = mockk<PayloadRepository>()
    private val launchRepoMock = mockk<LaunchRepository>()

    @Test
    fun fetchOnePayloadSuccess() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { payloadInternalMock }
        val result = payloadServiceMock.fetchOnePayload(payloadExternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(result)
    }

    @Test
    fun fetchOnePayloadFailures() {
        val payloadId = "abc"
        val firstResult = payloadService.fetchOnePayload(payloadId)
        assert(!firstResult)
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val secondResult = payloadServiceMock.fetchOnePayload(payloadExternalMock.id)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(!secondResult)
    }

    @Test
    fun makeAPICallSuccess() {
        val payloadId = payloadExternalMock.id
        val expectedName = payloadExternalMock.name
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
    fun makeAPICallFailures() {
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
    fun saveAndUpdatePayloadsTestSuccess() {
// Note: Can't mock launchRepo behavior b/c that repo isn't part of PayloadService constructor,
//  so have to save required data in H2 first before can test this saveAndUpdate method
        prepForSaveAndUpdate()
        val payloads = listOf(payloadInternalMock)
        payloadService.updateOrSavePayloads(payloads)
        val firstResult = payloadRepo.findByIdOrNull(payloadInternalMock.id)
        var expectedName = payloadInternalMock.name
        assert(firstResult != null)
        assert(firstResult?.name == expectedName)
        payloadService.updateOrSavePayloads(listOf(editedPayloadInternalMock))
        expectedName = editedPayloadInternalMock.name
        val secondResult = payloadRepo.findByIdOrNull(editedPayloadInternalMock.id)
        assert(secondResult?.name == expectedName)
    }

    @Test
    fun savePayloadFailure_LaunchNotExist() {
        val payloads = listOf(payloadInternalMock)
        payloadService.updateOrSavePayloads(payloads)
        val foundPayload = payloadRepo.findByIdOrNull(payloads[0].id)
        assert(foundPayload == null)
    }

    fun prepForSaveAndUpdate() {
        launchpadService.updateOrSaveLaunchpads(listOf(launchpadMock))
        launchService.updateOrSaveLaunch(launchExternalMock)
    }

    @Test
    fun getPayloadsByLaunchIdSuccess() {
        prepForSaveAndUpdate()
        payloadService.updateOrSavePayloads(listOf(editedPayloadInternalMock))
        val launchId = launchExternalMock.id
        val expectedName = payloadExternalMock.name
        val result = payloadService.getPayloadsByLaunchId(launchId)
        assert(result?.get(0)?.name == expectedName)
    }

    @Test
    fun getPayloadsByLaunchIdFailure_PayloadsNotExist() {
        val launchId = launchExternalMock.id
        val result = payloadService.getPayloadsByLaunchId(launchId)
        assert(result == null)
    }
}