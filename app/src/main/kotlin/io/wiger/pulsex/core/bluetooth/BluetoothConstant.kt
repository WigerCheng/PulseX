package io.wiger.pulsex.core.bluetooth

import java.util.UUID

object BluetoothConstant {
    val heartbeatServiceUUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val heartbeatMeasurementUUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    val clientCharacteristicConfigUUID: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
