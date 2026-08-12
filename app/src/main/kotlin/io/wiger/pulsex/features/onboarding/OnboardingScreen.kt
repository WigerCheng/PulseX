@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)

package io.wiger.pulsex.features.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import io.wiger.pulsex.R
import io.wiger.pulsex.core.bluetooth.BluetoothUtil
import io.wiger.pulsex.core.notification.NotificationProvider
import io.wiger.pulsex.ui.PulseXIcons
import io.wiger.pulsex.ui.theme.PulseXTheme

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            viewModel.onIntent(OnboardingIntent.CompleteOnboarding)
            onFinish()
        }
    }

    if (uiState.isFinished) return

    val currentStep = uiState.steps.getOrNull(uiState.currentStepIndex) ?: return
    val totalSteps = uiState.steps.size

    BackHandler(uiState.currentStepIndex != 0) {
        viewModel.onIntent(OnboardingIntent.PreviousStep)
    }

    //蓝牙权限状态 (Accompanist 需要在 Composable 中声明)
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val bluetoothPermissionState = rememberMultiplePermissionsState(permissions = bluetoothPermissions)

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    LaunchedEffect(bluetoothPermissionState.allPermissionsGranted, notificationPermissionState?.status) {
        viewModel.onIntent(OnboardingIntent.RefreshSystem)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentStep = uiState.currentStepIndex,
                totalStep = totalSteps,
                buttonEnabled = when (currentStep) {
                    OnboardingStep.BluetoothPermission -> uiState.hasBluetoothPermission
                    OnboardingStep.BluetoothSwitch -> uiState.isBluetoothEnabled
                    else -> true
                },
                onNext = { viewModel.onIntent(OnboardingIntent.NextStep) }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            },
            modifier = Modifier.padding(innerPadding),
            label = "OnboardingStep"
        ) { step ->
            when (step) {
                OnboardingStep.BluetoothPermission -> BluetoothPermissionContent(
                    bluetoothPermissionState = bluetoothPermissionState,
                    hasBluetoothPermission = uiState.hasBluetoothPermission
                )

                OnboardingStep.BluetoothSwitch -> BluetoothSwitchContent(
                    isBluetoothEnabled = uiState.isBluetoothEnabled,
                )

                OnboardingStep.NotificationPermission -> NotificationPermissionContent(
                    hasNotificationPermission = uiState.hasNotificationPermission
                )

                OnboardingStep.LiveUpdatePermission -> LiveUpdatePermissionContent(
                    hasLiveUpdate = uiState.hasLiveUpdatePermission
                )
            }
        }
    }
}


@Composable
private fun BluetoothPermissionContent(
    bluetoothPermissionState: MultiplePermissionsState,
    hasBluetoothPermission: Boolean,
    modifier: Modifier = Modifier
) {
    OnboardingStepContent(
        title = stringResource(R.string.onboarding_bluetooth_permission_title),
        description = stringResource(R.string.onboarding_bluetooth_permission_desc),
        iconRes = R.drawable.ic_bluetooth_permission,
        buttonText = if (hasBluetoothPermission) stringResource(R.string.onboarding_permission_granted) else stringResource(
            R.string.onboarding_grant_bluetooth_permission
        ),
        onButtonClick = bluetoothPermissionState::launchMultiplePermissionRequest,
        buttonEnabled = !hasBluetoothPermission,
        modifier = modifier
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun BluetoothSwitchContent(
    isBluetoothEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    OnboardingStepContent(
        title = stringResource(R.string.onboarding_enable_bluetooth_title),
        description = stringResource(R.string.onboarding_enable_bluetooth_desc),
        iconRes = R.drawable.ic_bluetooth_switch,
        buttonText = if (isBluetoothEnabled) stringResource(R.string.onboarding_bluetooth_enabled) else stringResource(
            R.string.onboarding_enable_bluetooth_button
        ),
        onButtonClick = {
            BluetoothUtil.enableBluetooth(context)
        },
        buttonEnabled = !isBluetoothEnabled,
        modifier = modifier
    )
}

@Composable
private fun NotificationPermissionContent(
    hasNotificationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    OnboardingStepContent(
        title = stringResource(R.string.onboarding_notification_permission_title),
        description = stringResource(R.string.onboarding_notification_permission_desc),
        iconRes = R.drawable.ic_notification_permission,
        buttonText = if (hasNotificationPermission) stringResource(R.string.onboarding_permission_granted) else stringResource(
            R.string.onboarding_grant_notification_permission
        ),
        onButtonClick = {
            notificationPermissionState?.launchPermissionRequest()
        },
        buttonEnabled = !hasNotificationPermission,
        modifier = modifier
    )
}

@Composable
private fun LiveUpdatePermissionContent(
    hasLiveUpdate: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    OnboardingStepContent(
        title = stringResource(R.string.onboarding_live_update_permission_title),
        description = stringResource(R.string.onboarding_live_update_permission_desc),
        iconRes = R.drawable.ic_android16_live_notification,
        buttonText = if (hasLiveUpdate) stringResource(R.string.onboarding_live_update_permission_granted) else stringResource(
            R.string.onboarding_grant_live_update_permission
        ),
        onButtonClick = {
            NotificationProvider.startPromotionNotificationSetting(context)
        },
        buttonEnabled = !hasLiveUpdate,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun OnboardingStepContentPreview() {
    PulseXTheme {
        OnboardingStepContent(
            title = stringResource(R.string.onboarding_live_update_permission_title),
            description = stringResource(R.string.onboarding_live_update_permission_desc),
            iconRes = R.drawable.ic_android16_live_notification,
            buttonText = stringResource(R.string.onboarding_grant_live_update_permission),
            onButtonClick = { },
            buttonEnabled = true
        )
    }
}

@Composable
private fun OnboardingStepContent(
    title: String,
    description: String,
    iconRes: Int,
    buttonText: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLargeEmphasized,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Image(
            painter = painterResource(iconRes),
            modifier = Modifier
                .size(160.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.extraExtraLarge
                ),
            contentDescription = null,
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onButtonClick,
            shapes = ButtonDefaults.shapes(),
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Text(text = buttonText, style = MaterialTheme.typography.titleMediumEmphasized)
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomNavigationBarPreview() {
    PulseXTheme {
        BottomNavigationBar(
            1,
            5,
            true,
            {}
        )
    }
}

@Composable
private fun BottomNavigationBar(
    currentStep: Int,
    totalStep: Int,
    buttonEnabled: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(32.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(
            targetState = currentStep,
            label = "step",
            transitionSpec = {
                val o = this.initialState
                val n = this.targetState
                if (o > n) {
                    slideInVertically(initialOffsetY = { it / 2 }) togetherWith
                            ExitTransition.None
                } else {
                    slideInVertically() togetherWith ExitTransition.None
                }
            }) {
            Text(
                text = stringResource(R.string.onboarding_step_count, it + 1, totalStep),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        IconButton(
            onClick = onNext,
            shapes = IconButtonDefaults.shapes(),
            enabled = buttonEnabled,
            modifier = Modifier.size(IconButtonDefaults.largeContainerSize()),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        ) {
            Icon(
                imageVector = PulseXIcons.ArrowForward,
                contentDescription = stringResource(R.string.onboarding_next_step),
            )
        }
    }
}
