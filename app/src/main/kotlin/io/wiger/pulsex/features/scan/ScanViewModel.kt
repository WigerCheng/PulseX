package io.wiger.pulsex.features.scan

import android.annotation.SuppressLint
import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.core.bluetooth.BluetoothConstant
import io.wiger.pulsex.core.bluetooth.BluetoothScanner
import io.wiger.pulsex.core.system.SystemProvider
import io.wiger.pulsex.data.local.db.ScanResultDao
import io.wiger.pulsex.data.local.db.ScanResultEntity
import io.wiger.pulsex.data.local.pref.AppPreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val results: List<ScanResultEntity> = emptyList(),
    val selectedDeviceAddress: String? = null
)

sealed class ScanIntent {
    data object StartScanning : ScanIntent()
    data object StopScanning : ScanIntent()
    data class SelectDevice(val address: String, val name: String) : ScanIntent()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val appPreference: DataStore<AppPreference>,
    private val scanResultDao: ScanResultDao,
    private val bluetoothScanner: BluetoothScanner,
    systemProvider: SystemProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var timeoutJob: Job? = null
    private var lastDeviceFoundTime = 0L

    init {
        combine(
            scanResultDao.getAllResults(),
            appPreference.data,
            systemProvider.isBluetoothEnabledFlow
        ) { results, pref, isEnabled ->
            _uiState.update {
                it.copy(
                    results = results,
                    selectedDeviceAddress = pref.deviceAddress,
                    isBluetoothEnabled = isEnabled
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.StartScanning -> startScan()
            is ScanIntent.StopScanning -> stopScan()
            is ScanIntent.SelectDevice -> saveDevice(intent.address, intent.name)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (_uiState.value.isScanning) return

        _uiState.update { it.copy(isScanning = true) }
        lastDeviceFoundTime = System.currentTimeMillis()
        viewModelScope.launch { scanResultDao.clearAll() }
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            bluetoothScanner.scanDevices(
                scanFilters = listOf(
                    ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid(BluetoothConstant.heartbeatServiceUUID))
                        .build()
                )
            ).collect { result ->
                val name = result.scanRecord?.deviceName ?: result.device.name
                if (!name.isNullOrBlank()) {
                    lastDeviceFoundTime = System.currentTimeMillis()
                    scanResultDao.insertResult(
                        ScanResultEntity(
                            address = result.device.address,
                            name = name
                        )
                    )
                }
            }
        }

        startTimeoutCheck()
    }

    private fun startTimeoutCheck() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (System.currentTimeMillis() - lastDeviceFoundTime > 30000) {
                    stopScan()
                    break
                }
            }
        }
    }

    private fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }
        scanJob?.cancel()
        scanJob = null
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun saveDevice(address: String, name: String) {
        viewModelScope.launch {
            appPreference.updateData { preference ->
                preference.copy(
                    deviceAddress = address,
                    deviceName = name
                )
            }
        }
    }

    override fun onCleared() {
        stopScan()
    }
}
