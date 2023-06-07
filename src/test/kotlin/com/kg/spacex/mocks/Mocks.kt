package com.kg.spacex.mocks

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.models.payload.PayloadExternal

val launchInternalMock: LaunchInternal = LaunchInternal(
    name = "CRS-2",
    details = "Last launch of the original Falcon 9 v1.0 launch vehicle",
    date_utc = "2013-03-01T19:10:00.000Z",
    success = true,
    failures = emptyList(),
    id = "5eb87ce1ffd86e000604b333",
    launchpadId = "5e9e4501f509094ba4566f84",
    payloadIds = listOf("5eb0e4bbb6c3bb0006eeb1ed"),
    capsuleIds = listOf("5e9e2c5bf359189ef23b2667")
)

val launchpadMock: Launchpad = Launchpad(
    id = "5e9e4501f509094ba4566f84",
    full_name = "Cape Canaveral Space Force Station Space Launch Complex 40",
    locality = "Cape Canaveral",
    region = "Florida",
    status = "active",
    details = "SpaceX's primary Falcon 9 pad, where all east coast Falcon 9s launched prior to the AMOS-6 anomaly. " +
            "Previously used alongside SLC-41 to launch Titan rockets for the US Air Force, the pad was heavily " +
            "damaged by the AMOS-6 anomaly in September 2016. It returned to flight with CRS-13 on December 15, " +
            "2017, boasting an upgraded throwback-style Transporter-Erector modeled after that at LC-39A.",
    launch_attempts = 99,
    launch_successes = 97
)

val payloadsMock: List<PayloadExternal> = listOf(
    PayloadExternal(
        id = "5eb0e4bbb6c3bb0006eeb1ed",
        name = "CRS-2",
        type = "Dragon 1.0",
        regime = "low-earth",
        launchId = "5e9e4501f509094ba4566f84",
        customers = "NASA (CRS)",
        nationalities = "United States",
        manufacturers = "SpaceX",
        mass_kg = 677f,
        mass_lbs = 1492f
    )
)

val capsulesMock: List<Capsule> = listOf(
    Capsule(
        serial = "C104",
        id = "5e9e2c5bf359189ef23b2667",
        status = "unknown",
        last_update = "Location and status unknown",
        water_landings = 1,
        land_landings = 0
    )
)

val launchExternalMock: LaunchExternal = LaunchExternal(
    name = launchInternalMock.name,
    details = launchInternalMock.details,
    date_utc = launchInternalMock.date_utc,
    success = launchInternalMock.success,
    failures = launchInternalMock.failures,
    id = launchInternalMock.id,
    launchpad = launchpadMock,
    payloads = payloadsMock,
    capsules = capsulesMock
)
