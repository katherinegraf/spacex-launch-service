package com.kg.spacex.mocks

import com.kg.spacex.models.capsule.CapsuleInternal
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.capsule.CapsuleExternal
import com.kg.spacex.models.launch.LaunchCapsule
import com.kg.spacex.models.launch.LaunchExternal
import com.kg.spacex.models.launch.LaunchInternal
import com.kg.spacex.models.payload.PayloadExternal
import com.kg.spacex.models.payload.PayloadInternal
import java.time.LocalDate

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

val editedLaunchpadMock: Launchpad = Launchpad(
    id = launchpadMock.id,
    full_name = launchpadMock.full_name,
    locality = launchpadMock.locality,
    region = launchpadMock.region,
    status = launchpadMock.status,
    details = "abcdefg",
    launch_attempts = launchpadMock.launch_attempts,
    launch_successes = launchpadMock.launch_successes
)

val payloadInternalMock: PayloadInternal = PayloadInternal(
    id = "5eb0e4bbb6c3bb0006eeb1ed",
    name = "COTS Demo Flight 2",
    type = "Dragon 1.0",
    regime = "low-earth",
    launchId = "5eb87cdfffd86e000604b331",
    customers = listOf("NASA(COTS)"),
    nationalities = listOf("United States"),
    manufacturers = listOf("SpaceX"),
    mass_kg = 525f,
    mass_lbs = 1157f
)

val editedPayloadInternalMock: PayloadInternal = PayloadInternal(
    id = payloadInternalMock.id,
    name = "abcdefg",
    type = payloadInternalMock.type,
    regime = payloadInternalMock.regime,
    launchId = payloadInternalMock.launchId,
    customers = payloadInternalMock.customers,
    nationalities = payloadInternalMock.nationalities,
    manufacturers = payloadInternalMock.manufacturers,
    mass_kg = payloadInternalMock.mass_kg,
    mass_lbs = payloadInternalMock.mass_lbs
)

val payloadExternalMock: PayloadExternal = PayloadExternal(
    id = payloadInternalMock.id,
    name = "abcdefg",
    type = payloadInternalMock.type,
    regime = payloadInternalMock.regime,
    launchId = payloadInternalMock.launchId,
    customers = payloadInternalMock.customers[0],
    nationalities = payloadInternalMock.nationalities[0],
    manufacturers = payloadInternalMock.manufacturers[0],
    mass_kg = payloadInternalMock.mass_kg,
    mass_lbs = payloadInternalMock.mass_lbs
)

val capsuleInternalMock: CapsuleInternal = CapsuleInternal(
    id = "5e9e2c5bf359189ef23b2667",
    serial = "C104",
    type = "Dragon 1.0",
    status = "unknown",
    last_update = "Location and status unknown",
    water_landings = 1,
    land_landings = 0,
    launchIds = listOf("5eb87ce1ffd86e000604b333")
)

val editedCapsuleInternalMock: CapsuleInternal = CapsuleInternal(
    id = capsuleInternalMock.id,
    serial = capsuleInternalMock.serial,
    type = capsuleInternalMock.type,
    status = capsuleInternalMock.status,
    last_update = "abcdefg",
    water_landings = capsuleInternalMock.water_landings,
    land_landings = capsuleInternalMock.land_landings,
    launchIds = capsuleInternalMock.launchIds
)

val capsuleExternalMock: CapsuleExternal = CapsuleExternal(
    id = "5e9e2c5bf359189ef23b2667",
    serial = "C104",
    type = "Dragon 1.0",
    status = "unknown",
    last_update = "Location and status unknown",
    water_landings = 1,
    land_landings = 0,
)

val launchCapsulesMock: List<LaunchCapsule> = listOf(LaunchCapsule(
    launchId = capsuleInternalMock.launchIds[0],
    capsuleId = "abc"
))

val launchInternalMock: LaunchInternal = LaunchInternal(
    name = "COTS 2",
    details = "Launch was scrubbed on first attempt, second launch attempt was successful",
    date_utc = "2012-05-22T07:44:00.000Z",
    success = true,
    failures = emptyList(),
    id = "5eb87cdfffd86e000604b331",
    launchpadId = "5e9e4501f509094ba4566f84",
    payloadIds = listOf("5eb0e4bab6c3bb0006eeb1ea"),
    capsuleIds = listOf("5e9e2c5bf3591882af3b2665")
)

val launchExternalMock: LaunchExternal = LaunchExternal(
    name = launchInternalMock.name,
    details = launchInternalMock.details,
    date_utc = launchInternalMock.date_utc,
    success = launchInternalMock.success,
    failures = emptyList(),
    id = launchInternalMock.id,
    launchpad = launchpadMock,
    payloads = listOf(payloadExternalMock),
    capsules = listOf(capsuleExternalMock),
    updated_at = LocalDate.now()
)
