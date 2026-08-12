package io.wiger.pulsex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey val address: String,
    val name: String,
    val lastSeen: Long = System.currentTimeMillis()
)
