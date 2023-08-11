package com.kg.spacex.services

import com.kg.spacex.models.Launchpad
import com.kg.spacex.utils.ResourceUnavailableException
import com.kg.spacex.utils.SPACEX_API_LAUNCHPADS_URL
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpaceXAPIServiceTests {

    @Autowired
    private lateinit var spaceXAPIService: SpaceXAPIService

    @Test
    fun `should return collection of type Any when calling API endpoint without unique identifier`() {
        // when
        val result = spaceXAPIService.handleAPICall(
            SPACEX_API_LAUNCHPADS_URL,
            Launchpad.ArrayDeserializer()
        )
        // then
        assert(result is Array<*>)
    }

    @Test
    fun `should throw ResourceNotFoundException when API call fails`() {
        // given
        val invalidId = "invalid_id"

        // when / then
        assertThrows<ResourceUnavailableException> {
            spaceXAPIService.handleAPICall(
                SPACEX_API_LAUNCHPADS_URL.plus(invalidId),
                Launchpad.Deserializer()
            )
        }
    }
}
