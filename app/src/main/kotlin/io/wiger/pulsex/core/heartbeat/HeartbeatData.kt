package io.wiger.pulsex.core.heartbeat

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class HeartRateData(
    val heartRate: Int,
    val sensorContactSupported: Boolean,
    val skinContactDetected: Boolean,
    val energyExpended: Int?,
    val rrIntervals: List<Float>
)

fun parseHeartRateMeasurement(data: ByteArray): HeartRateData {
    // 使用 ByteBuffer 处理小端序（Little-Endian）
    val buffer = ByteBuffer
        .wrap(data).order(ByteOrder.LITTLE_ENDIAN)

    val flags = buffer.get().toInt()

    // 1. 解析心率值格式 (Bit 0)
    val is16BitRpm = (flags and 0x01) != 0
    val heartRate = if (is16BitRpm) {
        buffer.short.toInt() and 0xFFFF
    } else {
        buffer.get().toInt() and 0xFF
    }

    // 2. 解析皮肤接触状态 (Bit 1, 2)
    val contactSupport = (flags and 0x06) shr 1
    val sensorContactSupported = contactSupport == 2 || contactSupport == 3
    val skinContactDetected = contactSupport == 3

    // 3. 解析能量消耗 (Bit 3)
    val hasEnergyExpended = (flags and 0x08) != 0
    val energyExpended = if (hasEnergyExpended) {
        buffer.short.toInt() and 0xFFFF
    } else {
        null
    }

    // 4. 解析 RR 间期 (Bit 4)
    val hasRrIntervals = (flags and 0x10) != 0
    val rrIntervals = mutableListOf<Float>()
    if (hasRrIntervals) {
        // 循环读取剩下的字节，直到 buffer 读完
        while (buffer.remaining() >= 2) {
            val rrRaw = buffer.short.toInt() and 0xFFFF
            // 单位：1/1024 秒，转换为毫秒或秒
            val rrSeconds = rrRaw / 1024.0f
            rrIntervals.add(rrSeconds)
        }
    }

    return HeartRateData(
        heartRate = heartRate,
        sensorContactSupported = sensorContactSupported,
        skinContactDetected = skinContactDetected,
        energyExpended = energyExpended,
        rrIntervals = rrIntervals
    )
}
