package io.wiger.pulsex.core.bluetooth

import android.util.Log
import io.wiger.pulsex.data.local.db.BluetoothLogDao
import io.wiger.pulsex.data.local.db.BluetoothLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothLogger @Inject constructor(
    private val logDao: BluetoothLogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        saveLog("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message\n${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        Log.e(tag, fullMessage)
        saveLog("ERROR", tag, fullMessage)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        saveLog("WARN", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        saveLog("INFO", tag, message)
    }

    private fun saveLog(level: String, tag: String, message: String) {
        scope.launch {
            logDao.insertLog(
                BluetoothLogEntity(
                    level = level,
                    tag = tag,
                    message = message
                )
            )
        }
    }
}
