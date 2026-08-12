package io.wiger.pulsex.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothLogDao {
    @Query("SELECT * FROM bluetooth_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BluetoothLogEntity>>

    @Insert
    suspend fun insertLog(log: BluetoothLogEntity)

    @Query("DELETE FROM bluetooth_logs")
    suspend fun clearAll()
}
