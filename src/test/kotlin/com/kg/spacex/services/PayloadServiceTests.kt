package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.repos.FailureRepository
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.PayloadRepository
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@AutoConfigureTestDatabase
class PayloadServiceTests @Autowired constructor (
    private val service: PayloadService,
    private val repo: PayloadRepository,
    private val launchService: LaunchService,
    private val launchRepo: LaunchRepository,
    private val launchpadService: LaunchpadService,
    private val launchpadRepo: LaunchRepository,
    private val failureRepo: FailureRepository
)  {

    private val mockApiService = mockk<SpaceXAPIService>()
    private val mockService = PayloadService(
        mockApiService,
        repo,
        launchRepo
    )

    @Nested
    @DisplayName("API Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FetchFromAPI {

        @Test
        fun `should return matching payload when calling API for a valid payload id`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns payloadInternalMock

            // when
            val result = mockService.fetchOne(payloadInternalMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result.name == payloadInternalMock.name)
        }

        @Test
        fun `should return list of PayloadInternals given successful API call - unit test`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns arrayOf(payloadInternalMock)

            // when
            val result = mockService.fetchAll()

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assertIs<List<PayloadInternal>>(result)
            assert(payloadInternalMock in result)
        }

        @Test
        fun `should return list of PayloadInternals if API call is successful - integration test`() {
            // given
            val resultNames = mutableListOf<String>()

            // when
            val result = service.fetchAll()
            result.forEach { resultNames.add(it.name) }

            // then
            assertIs<List<PayloadInternal>>(result)
            assert(payloadInternalMock.name in resultNames)
        }

        @Test
        fun `should throw ResourceUnavailableException when API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when / then
            assertThrows<ResourceUnavailableException> { mockService.fetchOne("id") }
            assertThrows<ResourceUnavailableException> { mockService.fetchAll() }
            verify(exactly = 2) { mockApiService.handleAPICall(any(), any()) }
        }
    }

    @Nested
    @DisplayName("Db Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DbOps {

        @BeforeAll
        fun setup() {
            launchpadService.saveOrUpdate(listOf(launchpadMock))
            launchService.saveOrUpdate(listOf(launchInternalMock))
            repo.deleteAll()
        }

        @AfterEach
        fun tearDownAfterEach() {
            repo.deleteAll()
        }

        @AfterAll
        fun tearDownAfterAll() {
            repo.deleteAll()
            failureRepo.deleteAll()
            launchRepo.deleteAll()
            launchpadRepo.deleteAll()
        }

        @Test
        fun `should save new payload if associated launch exists`() {
            // given
            assertNull(repo.findByIdOrNull(payloadInternalMock.id))

            // when
            service.saveOrUpdate(listOf(payloadInternalMock))

            // then
            val queriedResult = repo.findByIdOrNull(payloadInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.name == payloadInternalMock.name)
        }

        @Test
        fun `should not save new payload if associated launch not exists`() {
            // given
            assertNull(repo.findByIdOrNull(payloadMockWithUnsavedLaunch.id))
            assertNull(launchRepo.findByIdOrNull(payloadMockWithUnsavedLaunch.launchId))

            // when
            service.saveOrUpdate(listOf(payloadMockWithUnsavedLaunch))

            // then
            assertNull(repo.findByIdOrNull(payloadMockWithUnsavedLaunch.id))
        }

        @Test
        fun `should update payload record if already exists`() {
            // given
            service.saveOrUpdate(listOf(payloadInternalMock))
            val payload = repo.findByIdOrNull(payloadInternalMock.id)
            assertNotNull(payload)
            val originalMassKg = payload.mass_kg

            // when
            service.saveOrUpdate(listOf(payloadInternalMockEdited))

            // then
            val queriedResult = repo.findByIdOrNull(payloadInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.mass_kg != originalMassKg)
            assert(queriedResult.mass_kg == payloadInternalMockEdited.mass_kg)
        }

        @Test
        fun `should throw ResourceNotFoundException when db record not exists`() {
            // given
            val invalidId = "invalidId"

            // when / then
            assertThrows<ResourceNotFoundException> { service.getById(invalidId) }
        }

        @Test
        fun `should return list of PayloadExternals if launch and payload exist for given launch id`() {
            // given
            service.saveOrUpdate(listOf(payloadInternalMock))

            // when
            val result = service.getAllByLaunchId(launchInternalMock.id)

            // then
            assertIs<List<PayloadExternal>>(result)
            assert(result[0].id == payloadInternalMock.id)
        }

        @Test
        fun `should return empty list if no payloads exist for given launch id`() {
            // given
            val queryResult = repo.findByIdOrNull(launchInternalMock.payloadIds[0])
            assertNull(queryResult)

            // when
            val result = service.getAllByLaunchId(launchInternalMock.id)

            // then
            assert(result.isEmpty())
        }
    }
}
