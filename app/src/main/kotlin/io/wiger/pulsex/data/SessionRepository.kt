package io.wiger.pulsex.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.wiger.pulsex.core.heartbeat.heartbeatFlow
import io.wiger.pulsex.data.local.db.SessionDao
import io.wiger.pulsex.data.local.db.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    @ApplicationContext private val context: Context
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + Job())

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentHeartbeat = MutableStateFlow(0)
    val currentHeartbeat: StateFlow<Int> = _currentHeartbeat.asStateFlow()

    private val _minHeartRate = MutableStateFlow(0)
    val minHeartRate: StateFlow<Int> = _minHeartRate.asStateFlow()

    private val _maxHeartRate = MutableStateFlow(0)
    val maxHeartRate: StateFlow<Int> = _maxHeartRate.asStateFlow()

    private val _recordedHeartRates = MutableStateFlow<List<Int>>(emptyList())
    val recordedHeartRates: StateFlow<List<Int>> = _recordedHeartRates.asStateFlow()

    private var heartbeatJob: Job? = null
    private var startTimeStamp: Long = 0L
    private var currentTitle: String? = null

    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun startRecording(title: String? = null) {
        if (_isRecording.value) return
        _isRecording.value = true
        startTimeStamp = System.currentTimeMillis()
        currentTitle = title
        _recordedHeartRates.value = emptyList()
        _minHeartRate.value = 0
        _maxHeartRate.value = 0
        _currentHeartbeat.value = 0

        heartbeatJob = repositoryScope.launch {
            heartbeatFlow(context).collect { value ->
                if (value > 0) {
                    _currentHeartbeat.value = value
                    val currentList = _recordedHeartRates.value.toMutableList()
                    currentList.add(value)
                    _recordedHeartRates.value = currentList

                    if (_minHeartRate.value == 0 || value < _minHeartRate.value) {
                        _minHeartRate.value = value
                    }
                    if (value > _maxHeartRate.value) {
                        _maxHeartRate.value = value
                    }
                }
            }
        }
    }

    suspend fun stopRecording(): Long? {
        if (!_isRecording.value) return null
        _isRecording.value = false
        heartbeatJob?.cancel()
        heartbeatJob = null

        val endTimeStamp = System.currentTimeMillis()
        val rates = _recordedHeartRates.value
        if (rates.isEmpty()) {
            return null
        }

        val minVal = _minHeartRate.value
        val maxVal = _maxHeartRate.value
        val avgVal = rates.average().toInt()

        val entity = SessionEntity(
            title = currentTitle,
            startTime = startTimeStamp,
            endTime = endTimeStamp,
            minHeartRate = minVal,
            maxHeartRate = maxVal,
            avgHeartRate = avgVal,
            heartRates = rates
        )
        currentTitle = null

        return sessionDao.insertSession(entity)
    }

    suspend fun deleteSession(session: SessionEntity) {
        sessionDao.deleteSession(session)
    }

    suspend fun restoreSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun clearAllSessions() {
        sessionDao.clearAll()
    }
}
