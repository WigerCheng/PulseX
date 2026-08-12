package io.wiger.pulsex.features.home

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.core.bluetooth.getRemoteLeDevice
import io.wiger.pulsex.core.heartbeat.HeartbeatProvider
import io.wiger.pulsex.core.system.SystemProvider
import io.wiger.pulsex.data.SessionRepository
import io.wiger.pulsex.data.local.db.SessionEntity
import io.wiger.pulsex.data.local.pref.AppPreference
import io.wiger.pulsex.data.local.pref.PulseXState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    appPreferenceDataStore: DataStore<AppPreference>,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val sessionRepository: SessionRepository,
    systemProvider: SystemProvider
) : ViewModel() {

    private val preferenceFlow = appPreferenceDataStore.data

    private val heartbeatValueFlow = HeartbeatProvider.heartbeat

    private val recentSessionFlow = sessionRepository.allSessions.map { it.firstOrNull() }

    private val pulseXStateFlow = systemProvider.pulseXState

    private val _durationSeconds = MutableStateFlow(0L)
    private var timerJob: Job? = null

    private val recordingFlow = combine(
        sessionRepository.isRecording,
        sessionRepository.currentHeartbeat,
        sessionRepository.minHeartRate,
        sessionRepository.maxHeartRate,
        combine(sessionRepository.recordedHeartRates, _durationSeconds) { rates, duration ->
            rates to duration
        }
    ) { isRecording, current, min, max, (rates, duration) ->
        RecordingState(isRecording, current, min, max, rates, duration)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        preferenceFlow,
        heartbeatValueFlow,
        recentSessionFlow,
        pulseXStateFlow,
        recordingFlow
    ) { pref, hb, recent, pulseState, rec ->
        HomeUiState(
            deviceName = pref.deviceName,
            deviceAddress = pref.deviceAddress,
            remoteDevice = if (pref.deviceAddress.isBlank()) null else bluetoothAdapter?.getRemoteLeDevice(pref.deviceAddress),
            heartbeat = hb,
            recentSession = recent,
            isRecording = rec.isRecording,
            currentSessionHeartbeat = rec.currentHeartbeat,
            minHeartRate = rec.minHeartRate,
            maxHeartRate = rec.maxHeartRate,
            recordedRates = rec.recordedHeartRates,
            durationSeconds = rec.durationSeconds,
            pulseXState = pulseState
        )
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun startRecording(title: String? = null) {
        _durationSeconds.value = 0L
        sessionRepository.startRecording(title)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (sessionRepository.isRecording.value) {
                delay(1.seconds)
                _durationSeconds.value += 1
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            sessionRepository.stopRecording()
            timerJob?.cancel()
            timerJob = null
            _durationSeconds.value = 0L
        }
    }

    private data class RecordingState(
        val isRecording: Boolean,
        val currentHeartbeat: Int,
        val minHeartRate: Int,
        val maxHeartRate: Int,
        val recordedHeartRates: List<Int>,
        val durationSeconds: Long
    )
}

@Immutable
data class HomeUiState(
    val deviceAddress: String = "",
    val deviceName: String = "",
    val remoteDevice: BluetoothDevice? = null,
    val heartbeat: Int = 0,
    val recentSession: SessionEntity? = null,
    val isRecording: Boolean = false,
    val currentSessionHeartbeat: Int = 0,
    val minHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val recordedRates: List<Int> = emptyList(),
    val durationSeconds: Long = 0L,
    val pulseXState: PulseXState = PulseXState.BluetoothError()
)
