package com.kg.spacex.services

//import com.kg.spacex.mocks.*
//import com.kg.spacex.models.launch.LaunchInternal
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//
//@SpringBootTest
//class LaunchServiceTests {
//
//    @Autowired
//    private lateinit var launchService: LaunchService
//
//    private val apiServiceMock = mockk<SpaceXAPIService>()
//    private val launchpadService = LaunchpadService(apiServiceMock)
//    private val payloadService = PayloadService(apiServiceMock)
//    private val capsuleService = CapsuleService(apiServiceMock)
//    private val launchServiceMock = LaunchService(
//        capsuleService, launchpadService, payloadService, apiServiceMock
//    )
//
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
//}
