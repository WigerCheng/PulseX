package io.wiger.pulsex.features.settings

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.wiger.pulsex.R
import io.wiger.pulsex.core.bluetooth.BluetoothUtil
import io.wiger.pulsex.core.notification.NotificationProvider
import io.wiger.pulsex.widget.HeartbeatWidgetReceiver

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_permissions_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Bluetooth Permission & Status
            PermissionItem(
                title = stringResource(R.string.settings_bluetooth_status),
                statusText = if (uiState.hasBluetoothPermission) {
                    if (uiState.isBluetoothEnabled) stringResource(R.string.settings_status_on)
                    else stringResource(R.string.settings_status_off)
                } else {
                    stringResource(R.string.settings_status_denied)
                },
                isOk = uiState.hasBluetoothPermission && uiState.isBluetoothEnabled,
                onFixClick = {
                    if (!uiState.hasBluetoothPermission) {
                        // In a real app, this should probably navigate to a permission request or settings
                        BluetoothUtil.enableBluetooth(context)
                    } else if (!uiState.isBluetoothEnabled) {
                        BluetoothUtil.enableBluetooth(context)
                    }
                },
                actionText = if (!uiState.hasBluetoothPermission) stringResource(R.string.settings_grant_button)
                else stringResource(R.string.settings_enable_button)
            )

            // Notification Permission
            PermissionItem(
                title = stringResource(R.string.settings_notification_status),
                statusText = if (uiState.hasNotificationPermission) stringResource(R.string.settings_status_granted)
                else stringResource(R.string.settings_status_denied),
                isOk = uiState.hasNotificationPermission,
                onFixClick = {
                    NotificationProvider.startPromotionNotificationSetting(context)
                },
                actionText = stringResource(R.string.settings_grant_button)
            )

            // Live Update (Android 16+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                PermissionItem(
                    title = stringResource(R.string.settings_live_update_status),
                    statusText = if (uiState.hasLiveUpdatePermission) stringResource(R.string.settings_status_granted)
                    else stringResource(R.string.settings_status_denied),
                    isOk = uiState.hasLiveUpdatePermission,
                    onFixClick = {
                        NotificationProvider.startPromotionNotificationSetting(context)
                    },
                    actionText = stringResource(R.string.settings_enable_button)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Home Screen Widget
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    Text(
                        text = stringResource(R.string.settings_widget_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.settings_widget_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val myProvider =
                                        ComponentName(context, HeartbeatWidgetReceiver::class.java)
                                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.settings_add_widget_button))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_info_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_version_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = uiState.appVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    statusText: String,
    isOk: Boolean,
    onFixClick: () -> Unit,
    actionText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            if (!isOk) {
                Button(
                    onClick = onFixClick,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
