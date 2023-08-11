package com.kg.spacex.services

import com.kg.spacex.mocks.*
import com.kg.spacex.repos.FailureRepository
import com.kg.spacex.repos.LaunchRepository
import com.kg.spacex.repos.LaunchpadRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FailureServiceTests @Autowired constructor(
    private val service: FailureService,
    private val repo: FailureRepository,
    private val launchService: LaunchService,
    private val launchRepo: LaunchRepository,
    private val launchpadService: LaunchpadService,
    private val launchpadRepo: LaunchpadRepository
) {

    @BeforeAll
    fun setup() {
        repo.deleteAll()
        launchpadService.saveOrUpdate(listOf(launchpadMock))
        launchService.saveOrUpdate(listOf(launchInternalMock))
    }

    @AfterEach
    fun tearDownAfterEach() {
        repo.deleteAll()
    }

    @AfterAll
    fun tearDownAfterAll() {
        repo.deleteAll()
        launchRepo.deleteAll()
        launchpadRepo.deleteAll()
    }

    @Test
    fun `should save new failure`() {
        // given
        assert(repo.findAllByLaunchId(launchInternalMock.id).isEmpty())

        // when
        service.saveOrUpdate(listOf(failureExternalMock))

        // then
        val queriedResult = repo.findAllByLaunchId(launchInternalMock.id)
        assert(queriedResult.isNotEmpty())
        assert(queriedResult[0].time == failureExternalMock.time)
    }

    @Test
    fun `should return true if failure record already exists in db`() {
        // given
        val mockRepo = mockk<FailureRepository>()
        val mockService = FailureService(mockRepo)
        every { mockRepo.findAllByLaunchId(any()) } returns listOf(failureExternalMock)

        // when
        val result = mockService.alreadyExists(failureExternalMock)

        // then
        assertTrue(result)
    }

    @Test
    fun `should not save duplicate failure record if already exists`() {
        // given
        service.saveOrUpdate(listOf(failureExternalMock))
        val existingFailures = repo.findAllByLaunchId(launchInternalMock.id)
        assert(existingFailures.size == 1)
        assert(existingFailures[0].time == failureExternalMock.time)
        assert(existingFailures[0].altitude == failureExternalMock.altitude)
        assert(existingFailures[0].reason == failureExternalMock.reason)

        // when
        service.saveOrUpdate(listOf(failureExternalMock))

        // then
        assert(repo.findAllByLaunchId(launchInternalMock.id).size == 1)
    }
}

