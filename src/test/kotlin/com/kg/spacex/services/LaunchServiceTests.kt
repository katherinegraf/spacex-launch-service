package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.repos.*
import com.kg.spacex.utils.ResourceUnavailableException
import com.kg.spacex.utils.generateLocalDate
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LaunchServiceTests {

    // TODO finish writing Launch tests

    @Autowired
    private lateinit var launchService: LaunchService
    @Autowired
    private lateinit var db: LaunchRepository

    private val mockkApiService = mockk<SpaceXAPIService>()
    private val mockkCapsuleService = mockk<CapsuleService>(relaxed = true)
    private val mockkLaunchpadService = mockk<LaunchpadService>(relaxed = true)
    private val mockkPayloadService = mockk<PayloadService>(relaxed = true)
    private val mockkDb = mockk<LaunchRepository>()

    @Autowired
    private val launchpadService = LaunchpadService(mockkApiService)
    @Autowired
    private val payloadService = PayloadService(mockkApiService)
    @Autowired
    private val capsuleService = CapsuleService(mockkApiService)
    @Autowired
    private val failureService = FailureService()
    private val launchServiceWithMockkedApi = LaunchService(
        capsuleService, launchpadService, payloadService, mockkApiService, failureService
    )
    @Autowired
    val launchServiceWithMockks = LaunchService(
        mockkCapsuleService, mockkLaunchpadService, mockkPayloadService, mockkApiService, failureService
    )
    val mockkLaunchService = mockk<LaunchService>()

    // TODO -- write FailureService tests

    @BeforeEach
    fun clearDb(
        @Autowired capsuleRepo: CapsuleRepository,
        @Autowired failureRepo: FailureRepository,
        @Autowired launchCapsuleRepo: LaunchCapsuleRepository,
        @Autowired launchRepo: LaunchRepository,
        @Autowired launchpadRepo: LaunchpadRepository,
        @Autowired payloadRepo: PayloadRepository
    ) {
        failureRepo.deleteAll()
        launchCapsuleRepo.deleteAll()
        capsuleRepo.deleteAll()
        payloadRepo.deleteAll()
        launchRepo.deleteAll()
        launchpadRepo.deleteAll()
    }

    @Nested
    @DisplayName("API Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FetchFromAPI @Autowired constructor (private val service: LaunchService) {

        @Test
        fun `should return matching launch when calling API for a valid launch id`() {
            // given
            every {
                mockkApiService.handleAPICall(any(), any())
            } answers { launchInternalMock }

            // when
            val result = launchServiceWithMockkedApi.fetchOne(launchInternalMock.id)

            // then
            verify { mockkApiService.handleAPICall(any(), any()) }
            assert(result.name == launchInternalMock.name)
        }

        @Test
        fun `should return list of LaunchInternals if API call is successful`() {
            // given
            val resultNames = mutableListOf<String>()

            // when
            val result = service.fetchAll()
            result.forEach { resultNames.add(it.name) }

            // then
            assertIs<List<LaunchInternal>>(result)
            assert(launchInternalMock.name in resultNames)
        }

        @Test
        fun `should throw ResourceUnavailableException when API call returns null`() {
            // given
            every {
                mockkApiService.handleAPICall(any(), any())
            } answers { nothing }

            // when / then
            assertThrows<ResourceUnavailableException> { launchServiceWithMockkedApi.fetchOne("id") }
            assertThrows<ResourceUnavailableException> { launchServiceWithMockkedApi.fetchAll() }
            verify(exactly = 2) { mockkApiService.handleAPICall(any(), any()) }
        }

        @Test
        fun `should fetch all data for capsules, launchpads, payloads, and launches from API and save to db`() {
            // TODO
        }
    }

    @Nested
    @DisplayName("Db Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DbOps @Autowired constructor (
        private val service: LaunchService,
        private val db: LaunchRepository,
        private val mockkRepo: LaunchRepository = mockk<LaunchRepository>(),
        private val launchpadRepo: LaunchpadRepository,
        private val failureRepo: FailureRepository
    ) {

        @AfterEach
        fun tearDown() {
            failureRepo.deleteAll()
            db.deleteAll()
            launchpadRepo.deleteAll()
        }

        @Test
        fun `should save new launch and its failure(s)`() {
            // given
            launchpadService.saveOrUpdate(listOf(launchpadMock))
            assertNull(db.findByIdOrNull(launchInternalMock.id))
            assert(failureRepo.findAllByLaunchId(launchInternalMock.id).isEmpty())

            // when
            service.saveOrUpdate(listOf(launchInternalMock))

            // then
            val queriedLaunch = db.findByIdOrNull(launchInternalMock.id)
            assertNotNull(queriedLaunch)
            assert(queriedLaunch.name == launchInternalMock.name)
            val queriedFailures = failureRepo.findAllByLaunchId(launchInternalMock.id)
            assert(queriedFailures.isNotEmpty())
            assert(queriedFailures[0].reason == failureMock.reason)
        }

        @Test
        fun `should update launch record if already exists`() {
            // given
            launchpadService.saveOrUpdate(listOf(launchpadMock))
            service.saveOrUpdate(listOf(launchInternalMock))
            val launch = db.findByIdOrNull(launchInternalMock.id)
            assertNotNull(launch)
            val originalSuccessStatus = launch.success

            // when
            service.saveOrUpdate(listOf(launchInternalMockEdited))

            // then
            val queriedResult = db.findByIdOrNull(launchInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.success != originalSuccessStatus)
            assert(queriedResult.success == launchInternalMockEdited.success)
        }

        @Test
        fun `should return true if updated_at is null`() {
            // given
            val lastUpdated = db.findFirstByOrderById()?.updated_at
            assertNull(lastUpdated)

            // when
            val result = service.isDataRefreshNeeded()

            // then
            assertTrue(result)
        }

        // TODO
        @Test
        fun `should return true if updated_at is greater than 6 days ago`() {
            // given
            launchpadService.saveOrUpdate(listOf(launchpadMock))
            every {
                mockkRepo.findFirstByOrderById()
            } returns launchMockFromJanuary2023
            launchServiceWithMockks.saveOrUpdate(listOf(launchInternalMock))
            val lastUpdatedAt = db.findFirstByOrderById()?.updated_at

            // when
            val result = launchServiceWithMockks.isDataRefreshNeeded()

            // then
            verify { mockkRepo.findFirstByOrderById() }
            assertTrue(result)
        }

    }


    // OLD TESTS

//    @Test
//    fun refreshAllData_Success() {
//////        every {
//////            mockkApiService.handleAPICall(
//////                SPACEX_API_CAPSULES_URL,
//////                any()
////////                CapsuleInternal.ArrayDeserializer()
//////            )
//////        } answers { arrayOf(capsuleInternalMock) }
//////        every {
//////            mockkApiService.handleAPICall(
//////                SPACEX_API_LAUNCHPADS_URL,
//////                any()
////////                Launchpad.ArrayDeserializer()
//////            )
//////        } returns { arrayOf(launchpadMock) }
//////        every {
//////            mockkApiService.handleAPICall(
//////                SPACEX_API_LAUNCHES_URL,
//////                any()
////////                LaunchInternal.ArrayDeserializer()
//////            )
//////        } returns { arrayOf(launchInternalMock) }
//////        every {
//////            mockkApiService.handleAPICall(
//////                SPACEX_API_PAYLOADS_URL,
//////                any()
////////                PayloadInternal.ArrayDeserializer()
//////            )
//////        } returns { arrayOf(payloadInternalMock) }
//////        val result = launchServiceMock.refreshAllData()
//////        assert(result == true)
////
//        every { mockkCapsuleService.fetchAllCapsules() } answers { listOf(capsuleInternalMock) }
//        every { mockkLaunchpadService.fetchAllLaunchpads() } answers { listOf(launchpadMock) }
//        every { mockkApiService.handleAPICall(SPACEX_API_LAUNCHES_URL, any()) } answers { arrayOf(launchInternalMock) }
//        every { mockkPayloadService.fetchAllPayloads() } answers { listOf(payloadInternalMock) }
//        val result = launchServiceWithMockks.refreshAllData()
//        verify { mockkCapsuleService.fetchAllCapsules() }
//        verify { mockkLaunchpadService.fetchAllLaunchpads() }
//        verify { launchServiceWithMockks.fetchAllLaunches() }
//        verify { mockkPayloadService.fetchAllPayloads() }
////        val result = launchService.refreshAllData()
//        assert(result == true)
//    }

//    @Test
//    fun refreshAllData_fetchAllLaunches_Failure() {
//        /*
//        Expect returns null if any API call returns null
//         */
//        every {
//            mockkApiService.handleAPICall(any(), any())
//        } answers { nothing }
//        val firstResult = launchServiceWithMockkedApi.refreshAllData()
//        assert(firstResult == null)
//        val secondResult = launchServiceWithMockkedApi.fetchAllLaunches()
//        assert(secondResult == null)
//    }
//
//    @Test
//    fun prepareToSave_Success(
//    ) {
//        launchpadService.updateOrSaveLaunchpads(listOf(launchpadMock))
//        val result = launchService.prepareToSaveLaunch(launchInternalMock)
//        assert(result is LaunchExternal)
//    }


//    @Test
//    fun getOneLaunchExternalTestSuccess() {
//        val result = launchService.getOneLaunchExternal(launchExternalMock.id)
//        assert(result?.name == launchExternalMock.name)
//    }
//
//    @Test
//    fun getOneLaunchExternalTestFailure() {
//        every {
//            apiServiceMock.handleAPICall(any(), any())
//        } answers { nothing }
//        val launchId = launchInternalMock.id
//        val firstResult = launchServiceMock.getOneLaunchExternal(launchId)
//        verify { apiServiceMock.handleAPICall(any(), any()) }
//        assert(firstResult == null)
//        val invalidId = "abc"
//        val secondResult = launchService.getOneLaunchExternal(invalidId)
//        assert(secondResult == null)
//    }
//
//    @Test
//    fun getListOfLaunchExternalsTestSuccess() {
//        val result = launchService.getListOfLaunchExternals()
//        val resultIdList = mutableListOf<String>()
//        result?.forEach { launchExternal ->
//            resultIdList.add(launchExternal.id)
//        }
//        assert(result != null)
//        assert(launchExternalMock.id in resultIdList)
//    }
//
//    @Test
//    fun getListOfLaunchExternalsTestFailure() {
//        every {
//            apiServiceMock.handleAPICall(any(), any())
//        } answers { nothing }
//        val result = launchServiceMock.getListOfLaunchExternals()
//        verify { apiServiceMock.handleAPICall(any(), any()) }
//        assert(result == null)
//    }
//
//    @Test
//    fun fetchOneLaunchFromSpaceXTestSuccess() {
//        val expectedName = launchExternalMock.name
//        val result = launchService.fetchOneLaunchFromSpaceX(launchExternalMock.id)
//        assert(result?.name == expectedName)
//    }
//
//    @Test
//    fun fetchOneLaunchFromSpaceXTestFailure() {
//        every {
//            apiServiceMock.handleAPICall(any(), any())
//        } answers { nothing }
//        val launchId = launchInternalMock.id
//        val firstResult = launchServiceMock.fetchOneLaunchFromSpaceX(launchId)
//        verify { apiServiceMock.handleAPICall(any(), any()) }
//        assert(firstResult == null)
//        val invalidId = "abc"
//        val secondResult = launchService.fetchOneLaunchFromSpaceX(invalidId)
//        assert(secondResult == null)
//
//    }
//
//    @Test
//    fun fetchAllLaunchesFromSpaceXSuccess() {
//        val expectedLaunchId = launchExternalMock.id
//        val launchIds = mutableListOf<String>()
//        val result = launchService.fetchAllLaunchesFromSpaceX()
//        result?.forEach { launch ->
//            launchIds.add(launch.id)
//        }
//        assert(result != null)
//        assert(expectedLaunchId in launchIds)
//    }
//
//    @Test
//    fun fetchAllLaunchesFromSpaceXFailure() {
//        every {
//            apiServiceMock.handleAPICall(any(), any())
//        } answers { nothing }
//        val result = launchServiceMock.fetchAllLaunchesFromSpaceX()
//        verify { apiServiceMock.handleAPICall(any(), any()) }
//        assert(result == null)
//    }
//
//    @Test
//    fun fetchLaunchExternalForLaunchInternalTestSuccess() {
//        val expectedLaunchpad = launchpadMock
//        val expectedPayloads = payloadsMock
//        val expectedCapsules = capsulesMock
//        val result = launchService.fetchLaunchExternalForLaunchInternal(launchInternalMock)
//        assert(result?.launchpad?.region == expectedLaunchpad.region)
//        assert(result?.payloads?.get(0)?.customers == expectedPayloads[0].customers)
//        assert(result?.capsules?.get(0)?.serial == expectedCapsules[0].serial)
//    }
//
//    @Test
//    fun fetchLaunchExternalForLaunchInternalTestFailure() {
//        every {
//            apiServiceMock.handleAPICall(any(), any())
//        } answers { nothing }
//        val firstResult = launchServiceMock.fetchLaunchExternalForLaunchInternal(launchInternalMock)
//        verify { apiServiceMock.handleAPICall(any(), any()) }
//        assert(firstResult == null)
//        val invalidLaunchInternal = LaunchInternal(
//            name = "name",
//            details = null,
//            date_utc = "date",
//            success = false,
//            failures = emptyList(),
//            id = "abc",
//            launchpad = "abc",
//            payloads = listOf("abc"),
//            capsules = listOf("abc")
//        )
//        val secondResult = launchService.fetchLaunchExternalForLaunchInternal(invalidLaunchInternal)
//        assert(secondResult == null)
//    }
}
