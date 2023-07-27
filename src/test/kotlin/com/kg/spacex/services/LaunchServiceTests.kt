package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.repos.*
import com.kg.spacex.utils.ResourceNotFoundException
import com.kg.spacex.utils.ResourceUnavailableException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import kotlin.test.*

@SpringBootTest
@AutoConfigureTestDatabase
class LaunchServiceTests @Autowired constructor(
    private val service: LaunchService,
    private val launchpadService: LaunchpadService,
    failureService: FailureService,
    private val failureRepo: FailureRepository,
    private val launchCapsuleRepo: LaunchCapsuleRepository,
    private val capsuleRepo: CapsuleRepository,
    private val payloadRepo: PayloadRepository,
    private val launchRepo: LaunchRepository,
    private val launchpadRepo: LaunchpadRepository
) {

    private val mockApiService = mockk<SpaceXAPIService>()
    private val mockCapsuleService = mockk<CapsuleService>()
    private val mockLaunchpadService = mockk<LaunchpadService>()
    private val mockPayloadService = mockk<PayloadService>()
    private val mockLaunchRepo = mockk<LaunchRepository>()

    val mockService = LaunchService(
        mockCapsuleService,
        mockLaunchpadService,
        mockPayloadService,
        mockApiService,
        failureService,
        mockLaunchRepo
    )

    @Nested
    @DisplayName("API Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FetchFromAPI {

        @AfterAll
        fun tearDown() {
            failureRepo.deleteAll()
            launchCapsuleRepo.deleteAll()
            capsuleRepo.deleteAll()
            payloadRepo.deleteAll()
            launchRepo.deleteAll()
            launchpadRepo.deleteAll()
        }

        @Test
        fun `should fetch data for all entities from API and save to db`() {
            // given
            assert(capsuleRepo.findAll().isEmpty())
            assert(failureRepo.findAll().isEmpty())
            assert(launchCapsuleRepo.findAll().isEmpty())
            assert(launchRepo.findAll().isEmpty())
            assert(launchpadRepo.findAll().isEmpty())
            assert(payloadRepo.findAll().isEmpty())

            // when
            service.refreshAllData()

            // then
            assert(capsuleRepo.findAll().isNotEmpty())
            assert(failureRepo.findAll().isNotEmpty())
            assert(launchCapsuleRepo.findAll().isNotEmpty())
            assert(launchRepo.findAll().isNotEmpty())
            assert(launchpadRepo.findAll().isNotEmpty())
            assert(payloadRepo.findAll().isNotEmpty())
        }

        @Test
        fun `should return matching launch when calling API for a valid launch id`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { launchInternalMock }

            // when
            val result = mockService.fetchOne(launchInternalMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result.name == launchInternalMock.name)
        }

        // TODO mock fetchAll() methods

        @Test
        fun `should return list of LaunchInternals if API call is successful`() {
            // given
            val resultNames = mutableListOf<String>()

            // when
            val result = service.fetchAll()
            result.forEach { resultNames.add(it.name) }

            // then
            assertIs<List<LaunchInternal>>(result)
            assertIs<LaunchInternal>(result[0])
            assert(launchInternalMock.name in resultNames)
        }

        @Test
        fun `should throw ResourceUnavailableException when API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when / then
            assertThrows<ResourceUnavailableException> { mockService.fetchOne("someId") }
            assertThrows<ResourceUnavailableException> { mockService.fetchAll() }
            verify(exactly = 2) { mockApiService.handleAPICall(any(), any()) }
        }

    }

    @Nested
    @DisplayName("Db Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DbOps {

        @BeforeEach
        fun setup() {
            launchpadService.saveOrUpdate(listOf(launchpadMock))
        }

        @AfterEach
        fun tearDown() {
            failureRepo.deleteAll()
            launchCapsuleRepo.deleteAll()
            capsuleRepo.deleteAll()
            payloadRepo.deleteAll()
            launchRepo.deleteAll()
            launchpadRepo.deleteAll()
        }

        @Test
        fun `should save new launch and its failure(s)`() {
            // given
            assertNull(launchRepo.findByIdOrNull(launchInternalMock.id))
            assert(failureRepo.findAllByLaunchId(launchInternalMock.id).isEmpty())

            // when
            service.saveOrUpdate(listOf(launchInternalMock))

            // then
            val queriedLaunch = launchRepo.findByIdOrNull(launchInternalMock.id)
            assertNotNull(queriedLaunch)
            assert(queriedLaunch.name == launchInternalMock.name)

            val queriedFailures = failureRepo.findAllByLaunchId(launchInternalMock.id)
            assert(queriedFailures.isNotEmpty())
            assert(queriedFailures[0].reason == failureMock.reason)
        }

        @Test
        fun `should update launch record if already exists`() {
            // given
            service.saveOrUpdate(listOf(launchInternalMock))
            val launch = launchRepo.findByIdOrNull(launchInternalMock.id)
            assertNotNull(launch)
            val originalSuccessStatus = launch.success

            // when
            service.saveOrUpdate(listOf(launchInternalMockEdited))

            // then
            val queriedResult = launchRepo.findByIdOrNull(launchInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.success != originalSuccessStatus)
            assert(queriedResult.success == launchInternalMockEdited.success)
        }

        @Test
        fun `should convert LaunchInternal to LaunchExternal with emptyLists for payloads and capsules`() {
            // given
            assert(launchInternalMock.payloadIds.isNotEmpty())
            assert(launchInternalMock.capsuleIds.isNotEmpty())

            // when
            val result = service.convertToExternal(launchInternalMock)

            // then
            assert(result.launchpad.full_name == launchpadMock.full_name)
            assert(result.payloads.isEmpty())
            assert(result.capsules.isEmpty())
            assertNotNull(result.updated_at)
        }

        @Test
        fun `should return list of LaunchExternals`() {
            // given
            service.saveOrUpdate(listOf(launchInternalMock))

            // when
            val result = service.getAll()

            // then
            assert(result.isNotEmpty())
            assert(result[0].name == launchExternalMock.name)
        }

        @Test
        fun `should throw ResourceNotFoundException if findById returns null`() {
            // given
            assertNull(launchRepo.findByIdOrNull("someId"))

            // when / then
            assertThrows<ResourceNotFoundException> { service.buildLaunchExternalById("someId")  }
        }

        @Test
        fun `should return true if updated_at is null`() {
            // given
            assertNull(launchRepo.findFirstByOrderById()?.updated_at)

            // when
            val result = service.isDataRefreshNeeded()

            // then
            assertTrue(result)
        }

        @Test
        fun `should return true if updated_at is greater than 6 days ago`() {
            // given
            every { mockLaunchRepo.findFirstByOrderById() } answers { launchMockFromJanuary1999 }

            // when
            val result = mockService.isDataRefreshNeeded()

            // then
            verify { mockLaunchRepo.findFirstByOrderById() }
            assertTrue(result)
        }

        @Test
        fun `should return false if updated_at within past 6 days`() {
            // given
            service.saveOrUpdate(listOf(launchInternalMock))

            // when
            val result = service.isDataRefreshNeeded()

            // then
            assertFalse(result)
        }
    }
}
