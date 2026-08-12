package io.wiger.pulsex.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ScanResultEntity::class, SessionEntity::class], version = 3, exportSchema = false)
@TypeConverters(SessionTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun sessionDao(): SessionDao
}

