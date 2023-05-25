package com.kg.spacex.services

import com.kg.spacex.mocks.launchInternalMock
import com.kg.spacex.mocks.payloadsMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PayloadServiceTests {

    @Autowired
    private lateinit var payloadService: PayloadService

    private val apiServiceMock = mockk<SpaceXAPIService>()
    private val payloadServiceMock = PayloadService(apiServiceMock)

    @Test
    fun fetchPayloadsTestSuccess() {
        val payloadIds = launchInternalMock.payloads
        val expectedName = payloadsMock[0].name
        val result = payloadService.fetchPayloads(payloadIds)
        assert(result != null)
        assert(result?.get(0)?.name == expectedName)
    }

    @Test
    fun fetchPayloadsTestFailure() {
        every {
            apiServiceMock.handleAPICall(any(), any())
        } answers { nothing }
        val payloadIds = launchInternalMock.payloads
        val firstResult = payloadServiceMock.fetchPayloads(payloadIds)
        verify { apiServiceMock.handleAPICall(any(), any()) }
        assert(firstResult == null)
        val invalidId = listOf("abc")
        val secondResult = payloadService.fetchPayloads(invalidId)
        assert(secondResult == null)
    }
}
