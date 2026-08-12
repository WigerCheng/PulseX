package io.wiger.pulsex.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.data.local.db.BluetoothLogDao
import io.wiger.pulsex.data.local.db.BluetoothLogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothLogViewModel @Inject constructor(
    private val logDao: BluetoothLogDao
) : ViewModel() {

    val logs: StateFlow<List<BluetoothLogEntity>> = logDao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearLogs() {
        viewModelScope.launch {
            logDao.clearAll()
        }
    }
}
