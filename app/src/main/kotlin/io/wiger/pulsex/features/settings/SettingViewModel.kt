package io.wiger.pulsex.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.core.info.AppInfoProvider
import io.wiger.pulsex.core.system.SystemProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingUiState(
    val isBluetoothEnabled: Boolean = false,
    val hasBluetoothPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasLiveUpdatePermission: Boolean = false,
    val appVersion: String = "",
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val systemProvider: SystemProvider,
    private val appInfoProvider: AppInfoProvider,
) : ViewModel() {

    val uiState: StateFlow<SettingUiState> = combine(
        systemProvider.isBluetoothEnabledFlow,
        systemProvider.hasBluetoothPermission,
        systemProvider.hasNotificationPermission,
        systemProvider.hasLiveUpdatePermission
    ) { isBtEnabled, isBtPermGranted, isNotifyPermGranted, isLiveUpdatePermGranted ->
        SettingUiState(
            isBluetoothEnabled = isBtEnabled,
            hasBluetoothPermission = isBtPermGranted,
            hasNotificationPermission = isNotifyPermGranted,
            hasLiveUpdatePermission = isLiveUpdatePermGranted,
            appVersion = appInfoProvider.version
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingUiState(appVersion = appInfoProvider.version)
    )

    fun refreshPermissions() {
        systemProvider.refresh()
    }
}
