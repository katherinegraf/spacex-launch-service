package com.kg.spacex.controllers

import com.kg.spacex.mocks.*
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.models.payload.PayloadInternal
import com.kg.spacex.repos.*
import com.kg.spacex.services.*
import com.kg.spacex.utils.ErrorMessage
import com.kg.spacex.utils.ResourceNotFoundException
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.*

@SpringBootTest
@AutoConfigureTestDatabase
class SpaceXControllerTests @Autowired constructor (
    private val spaceXAPIService: SpaceXAPIService,
    private val capsuleService: CapsuleService,
    private val launchService: LaunchService,
    private val launchpadService: LaunchpadService,
    private val payloadService: PayloadService,
    private val failureService: FailureService,
    private val failureRepo: FailureRepository,
    private val launchCapsuleRepo: LaunchCapsuleRepository,
    private val capsuleRepo: CapsuleRepository,
    private val payloadRepo: PayloadRepository,
    private val launchRepo: LaunchRepository,
    private val launchpadRepo: LaunchpadRepository
) {

    private val mockApiService = mockk<SpaceXAPIService>()
    private val mockLaunchRepo = mockk<LaunchRepository>()
    private val mockCapsuleService = CapsuleService(
        mockApiService,
        capsuleRepo,
        launchCapsuleRepo
    )
    private val mockLaunchService = LaunchService(
        capsuleService,
        launchpadService,
        payloadService,
        mockApiService,
        failureService,
        mockLaunchRepo
    )
    private val mockLaunchpadService = LaunchpadService(
        mockApiService,
        launchpadRepo
    )
    private val mockPayloadService = PayloadService(
        mockApiService,
        payloadRepo,
        launchRepo
    )
    private val mockController = SpaceXController(
        mockApiService,
        mockCapsuleService,
        mockLaunchService,
        mockLaunchpadService,
        mockPayloadService
    )

    @Nested
    @DisplayName("Get from DB")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetFromDBTests @Autowired constructor (private val controller: SpaceXController) {

        @BeforeAll
        fun setup() {
            capsuleService.saveOrUpdate(listOf(capsuleInternalMock))
            launchpadService.saveOrUpdate(listOf(launchpadMock))
            launchService.saveOrUpdate(listOf(launchInternalMock))
            payloadService.saveOrUpdate(listOf(payloadInternalMock))
        }

        @Test
        fun `should return list of LaunchExternals`() {
            // given
            assert(launchRepo.findAllByOrderById().isNotEmpty())

            // when
            val result = controller.index()

            // then
            assert(result is List<*>)
            result as List<*>
            assert(result[0] is LaunchExternal)
        }

        @Test
        fun `should return 404 ResponseEntity when no db records exist`() {
            // given
            every { mockLaunchRepo.findFirstByOrderById() } returns launchExternalMock
                // ^ tricking isDataRefreshNeeded into returning false so that refreshAllData() isn't called
            every { mockLaunchRepo.findAllByOrderById() } returns emptyList()
                // ^ mocking that no db records exist

            // when
            val result = mockController.index()

            // then
            assertThrows<ResourceNotFoundException> { mockLaunchService.getAll() }
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "Unable to retrieve launch data from database")
        }

        @Test
        fun `should return LaunchExternal matching given id when exists`() {
            // given
            assertNotNull(launchRepo.findByIdOrNull(launchInternalMock.id))

            // when
            val result = controller.getLaunchFromDb(launchInternalMock.id) as LaunchExternal

            // then
            assert(result.name == launchInternalMock.name)
        }

        @Test
        fun `should return 404 ResponseEntity when given id not exists in launch repo`() {
            // given
            assertNull(launchRepo.findByIdOrNull("someId"))

            // when
            val result = controller.getLaunchFromDb("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "Could not find that id in database")
        }

        @Test
        fun `should return CapsuleExternal matching given id when exists`() {
            // given
            assertNotNull(capsuleRepo.findByIdOrNull(capsuleInternalMock.id))

            // when
            val result = controller.getCapsuleFromDb(capsuleInternalMock.id) as CapsuleExternal

            // then
            assert(result.serial == capsuleInternalMock.serial)
        }

        @Test
        fun `should return 404 ResponseEntity when given id not exists in capsule repo`() {
            // given
            assertNull(capsuleRepo.findByIdOrNull("someId"))

            // when
            val result = controller.getCapsuleFromDb("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "Could not find that id in database")
        }

        @Test
        fun `should return PayloadExternal matching given id when exists`() {
            // given
            assertNotNull(payloadRepo.findByIdOrNull(payloadInternalMock.id))

            // when
            val result = controller.getPayloadFromDb(payloadInternalMock.id) as PayloadExternal

            // then
            assert(result.name == payloadInternalMock.name)
        }

        @Test
        fun `should return 404 ResponseEntity when given id not exists in payload repo`() {
            // given
            assertNull(payloadRepo.findByIdOrNull("someId"))

            // when
            val result = controller.getPayloadFromDb("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "Could not find that id in database")
        }

        @Test
        fun `should return Launchpad matching given id when exists`() {
            // given
            assertNotNull(launchpadRepo.findByIdOrNull(launchpadMock.id))

            // when
            val result = controller.getLaunchpadFromDb(launchpadMock.id) as Launchpad

            // then
            assert(result.full_name == launchpadMock.full_name)
        }

        @Test
        fun `should return 404 ResponseEntity when given id not exists in launchpad repo`() {
            // given
            assertNull(launchpadRepo.findByIdOrNull("someId"))

            // when
            val result = controller.getLaunchpadFromDb("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "Could not find that id in database")
        }
    }

    @Nested
    @DisplayName("Fetch from API")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class APIFetchTests () {

        @Test
        fun `should return LaunchInternal matching given id given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns launchInternalMock

            // when
            val result = mockController.getLaunchFromAPI(launchInternalMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result is LaunchInternal)
            result as LaunchInternal
            assert(result.name == launchInternalMock.name)
        }

        @Test
        fun `should return 404 ResponseEntity when single Launch API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getLaunchFromAPI("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return list of LaunchInternals given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns arrayOf(launchInternalMock)

            // when
            val result = mockController.getLaunchesFromAPI()

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assertIs<List<LaunchInternal>>(result)
            assert(launchInternalMock in result)
        }

        @Test
        fun `should return 404 ResponseEntity when Launches API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getLaunchesFromAPI()

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return CapsuleInternal matching given id given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns capsuleInternalMock

            // when
            val result = mockController.getCapsuleFromAPI(capsuleInternalMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result is CapsuleInternal)
            result as CapsuleInternal
            assert(result.serial == capsuleInternalMock.serial)
        }

        @Test
        fun `should return 404 ResponseEntity when single Capsule API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getCapsuleFromAPI("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return list of CapsuleInternals given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns arrayOf(capsuleInternalMock)

            // when
            val result = mockController.getCapsulesFromAPI()

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assertIs<List<CapsuleInternal>>(result)
            assert(capsuleInternalMock in result)
        }

        @Test
        fun `should return 404 ResponseEntity when Capsules API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getCapsulesFromAPI()

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return PayloadInternal matching given id given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns payloadInternalMock

            // when
            val result = mockController.getPayloadFromAPI(payloadInternalMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result is PayloadInternal)
            result as PayloadInternal
            assert(result.name == payloadInternalMock.name)
        }

        @Test
        fun `should return 404 ResponseEntity when single Payload API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // whe
            val result = mockController.getPayloadFromAPI("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return list of PayloadInternals given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns arrayOf(payloadInternalMock)

            // when
            val result = mockController.getPayloadsFromAPI()

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assertIs<List<PayloadInternal>>(result)
            assert(payloadInternalMock in result)
        }

        @Test
        fun `should return 404 ResponseEntity when Payloads API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getPayloadsFromAPI()

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return Launchpad matching given id given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns launchpadMock

            // when
            val result = mockController.getLaunchpadFromAPI(launchpadMock.id)

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assert(result is Launchpad)
            result as Launchpad
            assert(result.full_name == launchpadMock.full_name)
        }

        @Test
        fun `should return 404 ResponseEntity when single Launchpad API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getLaunchpadFromAPI("someId")

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

        @Test
        fun `should return list of Launchpads given successful API call`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } returns arrayOf(launchpadMock)

            // when
            val result = mockController.getLaunchpadsFromAPI()

            // then
            verify { mockApiService.handleAPICall(any(), any()) }
            assertIs<List<Launchpad>>(result)
            assert(launchpadMock in result)
        }

        @Test
        fun `should return 404 ResponseEntity when Launchpads API call returns null`() {
            // given
            every { mockApiService.handleAPICall(any(), any()) } answers { nothing }

            // when
            val result = mockController.getLaunchpadsFromAPI()

            // then
            assert(result is ResponseEntity<*>)
            result as ResponseEntity<*>
            assert(result.statusCode == HttpStatus.NOT_FOUND && result.body is ErrorMessage)
            val resultBody = result.body as ErrorMessage
            assert(resultBody.status == 404 && resultBody.message == "API resource unavailable")
        }

    }
}
