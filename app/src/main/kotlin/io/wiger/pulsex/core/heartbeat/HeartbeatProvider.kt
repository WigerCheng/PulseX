package io.wiger.pulsex.core.heartbeat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object HeartbeatProvider {
    private val _heartbeat = MutableStateFlow(0)
    val heartbeat: StateFlow<Int> = _heartbeat

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    fun updateHeartbeat(newHeartbeat: Int) {
        _heartbeat.value = newHeartbeat
    }

    fun updateConnectionState(connected: Boolean) {
        _isConnected.value = connected
    }
}
