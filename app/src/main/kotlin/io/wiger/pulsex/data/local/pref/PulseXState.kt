package io.wiger.pulsex.data.local.pref

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PulseXState {
    val isBluetoothEnabled: Boolean

    /**
     * 蓝牙异常（包括没权限和关了开关）
     */
    data class BluetoothError(
        override val isBluetoothEnabled: Boolean = false
    ) : PulseXState

    /**
     * 未连接到设备
     */
    data class Disconnected(
        override val isBluetoothEnabled: Boolean = true,
        val deviceName: String? = null,
        val deviceAddress: String? = null
    ) : PulseXState

    /**
     * 已连接并开始数据交互
     */
    data class Connected(
        val heartbeat: Int,
        override val isBluetoothEnabled: Boolean = true,
        val deviceName: String? = null,
        val deviceAddress: String? = null
    ) : PulseXState
}
