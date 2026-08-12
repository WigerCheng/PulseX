package io.wiger.pulsex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String? = null,
    val startTime: Long,
    val endTime: Long,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgHeartRate: Int,
    val heartRates: List<Int>
)
