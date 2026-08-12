package io.wiger.pulsex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bluetooth_logs")
data class BluetoothLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String,
    val tag: String,
    val message: String
)
