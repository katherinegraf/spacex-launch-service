package com.kg.spacex.models.launch

import com.kg.spacex.models.Capsule
import com.kg.spacex.models.Launchpad
import com.kg.spacex.models.Payload

class LaunchExternal (
    val name: String,
    val details: String?,
    val date_utc: String,
    val success: Boolean,
    val failures: List<Map<String, String>>,
    val id: String,
    val launchpad: Launchpad?,
    val payloads: List<Payload>?,
    val capsules: List<Capsule>?
)
