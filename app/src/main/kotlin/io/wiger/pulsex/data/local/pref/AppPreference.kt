package io.wiger.pulsex.data.local.pref

import kotlinx.serialization.Serializable

@Serializable
data class AppPreference(
    val deviceName: String = "",
    val deviceAddress: String = "",
    val isOnboardingCompleted: Boolean = false,
)
