package io.wiger.pulsex.features.onboarding

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.wiger.pulsex.core.system.SystemProvider
import io.wiger.pulsex.data.local.pref.AppPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    BluetoothPermission,
    BluetoothSwitch,
    NotificationPermission,
    LiveUpdatePermission
}

data class OnboardingUiState(
    val currentStepIndex: Int = 0,
    val steps: List<OnboardingStep> = emptyList(),
    val isFinished: Boolean = false,
    val hasBluetoothPermission: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasLiveUpdatePermission: Boolean = false
)

sealed interface OnboardingIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object RefreshSystem : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val systemProvider: SystemProvider,
    private val appPreference: DataStore<AppPreference>
) : ViewModel() {

    private val _currentStepIndex = MutableStateFlow(0)

    val uiState: StateFlow<OnboardingUiState> = combine(
        _currentStepIndex,
        systemProvider.isBluetoothEnabledFlow,
        systemProvider.hasBluetoothPermission,
        systemProvider.hasNotificationPermission,
        systemProvider.hasLiveUpdatePermission
    ) { index, isBtEnabled, hasBtPerm, hasNotifyPerm, hasLiveUpdatePerm ->
        val requiredSteps = buildList {
            if (!hasBtPerm) add(OnboardingStep.BluetoothPermission)
            if (!isBtEnabled) add(OnboardingStep.BluetoothSwitch)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifyPerm) {
                add(OnboardingStep.NotificationPermission)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && !hasLiveUpdatePerm) {
                add(OnboardingStep.LiveUpdatePermission)
            }
        }

        OnboardingUiState(
            currentStepIndex = index.coerceAtMost(if (requiredSteps.isEmpty()) 0 else requiredSteps.size - 1),
            steps = requiredSteps,
            isFinished = requiredSteps.isEmpty(),
            hasBluetoothPermission = hasBtPerm,
            isBluetoothEnabled = isBtEnabled,
            hasNotificationPermission = hasNotifyPerm,
            hasLiveUpdatePermission = hasLiveUpdatePerm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OnboardingUiState()
    )

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextStep -> {
                val state = uiState.value
                if (state.currentStepIndex < state.steps.size - 1) {
                    _currentStepIndex.update { it + 1 }
                } else {
                    // If we are at the last step, we can try to finish if conditions are met
                    // In many cases, isFinished will already be true if all conditions are met
                    // but this provides a manual fallback if the UI needs it.
                    onIntent(OnboardingIntent.CompleteOnboarding)
                }
            }
            OnboardingIntent.PreviousStep -> {
                if (_currentStepIndex.value > 0) {
                    _currentStepIndex.update { it - 1 }
                }
            }
            OnboardingIntent.RefreshSystem -> {
                systemProvider.refresh()
            }
            OnboardingIntent.CompleteOnboarding -> {
                viewModelScope.launch {
                    appPreference.updateData { it.copy(isOnboardingCompleted = true) }
                }
            }
        }
    }
}
