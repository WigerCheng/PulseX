package io.wiger.pulsex

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.core.bluetooth.getRemoteLeDevice
import io.wiger.pulsex.data.local.pref.AppPreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    appPreference: DataStore<AppPreference>,
    private val bluetoothAdapter: BluetoothAdapter?,
) : ViewModel() {

    val remoteDevice: StateFlow<BluetoothDevice?> = appPreference.data.map { pref ->
        if (pref.deviceAddress.isNotBlank()) {
            bluetoothAdapter?.getRemoteLeDevice(pref.deviceAddress)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isOnboardingCompleted: StateFlow<Boolean> = appPreference.data
        .map { it.isOnboardingCompleted }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true // Default to true to avoid flashing onboarding if already done
        )
}
