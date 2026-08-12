package io.wiger.pulsex.data.local.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionTypeConverters {
    @TypeConverter
    fun fromList(list: List<Int>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toList(value: String): List<Int> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
