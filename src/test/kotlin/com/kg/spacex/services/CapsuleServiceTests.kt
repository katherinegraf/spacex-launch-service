package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.repos.CapsuleRepository
import com.kg.spacex.repos.LaunchCapsuleRepository
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
class CapsuleServiceTests {

    private val mockkApiService = mockk<SpaceXAPIService>()
    private val mockService = CapsuleService(mockkApiService)

    @Nested
    @DisplayName("API Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FetchFromAPI @Autowired constructor (private val service: CapsuleService) {

        @Test
        fun `should return matching capsule when calling API for a valid capsule id`() {
            // given
            every {
                mockkApiService.handleAPICall(any(), any())
            } answers { capsuleInternalMock }

            // when
            val result = mockService.fetchOne(capsuleInternalMock.id)

            // then
            verify { mockkApiService.handleAPICall(any(), any()) }
            assert(result.serial == capsuleInternalMock.serial)
        }

        @Test
        fun `should return list of CapsuleInternals if API call is successful`() {
            // given
            val resultSerials = mutableListOf<String>()

            // when
            val result = service.fetchAll()
            result.forEach { resultSerials.add(it.serial) }

            // then
            assertIs<List<CapsuleInternal>>(result)
            assert(capsuleInternalMock.serial in resultSerials)
        }

        @Test
        fun `should throw ResourceUnavailableException when API call returns null`() {
            // given
            every {
                mockkApiService.handleAPICall(any(), any())
            } answers { nothing }

            // when / then
            assertThrows<ResourceUnavailableException> { mockService.fetchOne("id") }
            assertThrows<ResourceUnavailableException> { mockService.fetchAll() }
            verify(exactly = 2) { mockkApiService.handleAPICall(any(), any()) }
        }
    }

    @Nested
    @DisplayName("Db Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class DbOps @Autowired constructor (
        private val service: CapsuleService,
        private val db: CapsuleRepository,
        private val joinDb: LaunchCapsuleRepository
    ) {

        @AfterEach
        fun tearDown() {
            db.deleteAll()
            joinDb.deleteAll()
        }

        @Test
        fun `should save new capsule`() {
            // given
            assertNull(db.findByIdOrNull(capsuleInternalMock.id))

            // when
            service.saveOrUpdate(listOf(capsuleInternalMock))

            // then
            val queriedResult = db.findByIdOrNull(capsuleInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.serial == capsuleInternalMock.serial)
        }

        @Test
        fun `should update capsule record if already exists`() {
            // given
            service.saveOrUpdate(listOf(capsuleInternalMock))
            val capsule = db.findByIdOrNull(capsuleInternalMock.id)
            assertNotNull(capsule)
            val originalWaterLandingsCount = capsule.water_landings

            // when
            service.saveOrUpdate(listOf(capsuleInternalMockEdited))

            // then
            val queriedResult = db.findByIdOrNull(capsuleInternalMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.water_landings != originalWaterLandingsCount)
            assert(queriedResult.water_landings == capsuleInternalMockEdited.water_landings)
        }

        @Test
        fun `should not create duplicate launch-capsule association record in join table`() {
            // given
            service.updateLaunchCapsuleJoinTable(capsuleInternalMock)
            assert(joinDb.findAllByLaunchId(capsuleInternalMock.launchIds[0]).isNotEmpty())

            // when
            service.updateLaunchCapsuleJoinTable(capsuleInternalMock)

            // then
            val queriedResult = joinDb.findAllByLaunchId(capsuleInternalMock.launchIds[0])
            assert(queriedResult.size == 1)
        }

        @Test
        fun `should throw ResourceNotFoundException when db record not exists`() {
            // given
            val invalidId = "invalidId"

            // when / then
            assertThrows<ResourceNotFoundException> { service.getByIds(listOf(invalidId)) }
        }

        @Test
        fun `should return list of CapsuleExternals given valid launch id and existing LaunchCapsule record`() {
            // given
            service.saveOrUpdate(listOf(capsuleInternalMock))
            assert(joinDb.findAllByLaunchId(capsuleInternalMock.launchIds[0]).isNotEmpty())

            // when
            val result = service.getCapsulesForLaunch(launchInternalMock.id)

            // then
            assertIs<List<CapsuleExternal>>(result)
            assert(result[0].serial == capsuleExternalMock.serial)
        }

        @Test
        fun `should return empty list given invalid launch id or LaunchCapsule not exists`() {
            // given
            val invalidId = "invalidId"
            val validId = launchInternalMock.id
            assert(joinDb.findAllByLaunchId(capsuleInternalMock.launchIds[0]).isEmpty())

            // when
            val firstResult = service.getCapsulesForLaunch(invalidId)
            val secondResult = service.getCapsulesForLaunch(validId)

            // then
            assert(firstResult.isEmpty())
            assert(secondResult.isEmpty())
        }
    }
}
