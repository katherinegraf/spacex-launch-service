package com.kg.spacex.services

import com.kg.spacex.mocks.launchpadMock
import com.kg.spacex.mocks.launchpadMockEdited
import com.kg.spacex.models.Launchpad
import com.kg.spacex.repos.LaunchpadRepository
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
class LaunchpadServiceTests {

    private val mockkApiService = mockk<SpaceXAPIService>()
    val mockService = LaunchpadService(mockkApiService)

    @Nested
    @DisplayName("API Operations")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class FetchFromAPI @Autowired constructor (private val service: LaunchpadService) {

        @Test
        fun `should return matching launchpad when calling API for a valid launchpad id`() {
            // given
            every {
                mockkApiService.handleAPICall(any(), any())
            } answers { launchpadMock }

            // when
            val result = mockService.fetchOne(launchpadMock.id)

            // then
            verify { mockkApiService.handleAPICall(any(), any()) }
            assert(result.locality == launchpadMock.locality)
        }

        @Test
        fun `should return list of Launchpads if API call is successful`() {
            // given
            val resultLocalities = mutableListOf<String>()

            // when
            val result = service.fetchAll()
            result.forEach { resultLocalities.add(it.locality) }

            // then
            assertIs<List<Launchpad>>(result)
            assert(launchpadMock.locality in resultLocalities)
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
        private val service: LaunchpadService,
        private val db: LaunchpadRepository
    ) {

        @AfterEach
        fun tearDown() {
            db.deleteAll()
        }

        @Test
        fun `should save new launchpad`() {
            // given
            assertNull(db.findByIdOrNull(launchpadMock.id))

            // when
            service.saveOrUpdate(listOf(launchpadMock))

            // then
            val queriedResult = db.findByIdOrNull(launchpadMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.details == launchpadMock.details)
        }

        @Test
        fun `should update launchpad record if already exists`() {
            // given
            service.saveOrUpdate(listOf(launchpadMock))
            val launchpad = db.findByIdOrNull(launchpadMock.id)
            assertNotNull(launchpad)
            val originalAttemptCount = launchpad.launch_attempts

            // when
            service.saveOrUpdate(listOf(launchpadMockEdited))

            // then
            val queriedResult = db.findByIdOrNull(launchpadMock.id)
            assertNotNull(queriedResult)
            assert(queriedResult.launch_attempts != originalAttemptCount)
            assert(queriedResult.launch_attempts == launchpadMockEdited.launch_attempts)
        }

        @Test
        fun `should throw ResourceNotFoundException if record not exists`() {
            // given
            val invalidId = "invalidId"

            // when / then
            assertThrows<ResourceNotFoundException> { service.getById(invalidId) }
        }
    }
}
