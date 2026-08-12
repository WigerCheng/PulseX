package io.wiger.pulsex.features.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.wiger.pulsex.R
import io.wiger.pulsex.core.bluetooth.BluetoothUtil
import io.wiger.pulsex.core.bluetooth.getDisplayAddress
import io.wiger.pulsex.core.bluetooth.getDisplayName
import io.wiger.pulsex.data.local.db.SessionEntity
import io.wiger.pulsex.data.local.pref.PulseXState
import io.wiger.pulsex.ui.PulseXIcons
import io.wiger.pulsex.ui.component.HeartbeatCard
import io.wiger.pulsex.ui.component.SessionControlCard
import io.wiger.pulsex.ui.component.StatItem
import io.wiger.pulsex.ui.component.formatDuration
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    toDevicesPage: () -> Unit,
    onConnectClick: (BluetoothDevice) -> Unit = {},
    onDisconnectClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val globalState = state.pulseXState
    val isConnected = globalState is PulseXState.Connected
    val isBluetoothEnabled = globalState.isBluetoothEnabled
    val context = LocalContext.current
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showNamingDialog by remember { mutableStateOf(false) }
    var sessionName by remember { mutableStateOf("") }

    if (showNamingDialog) {
        AlertDialog(
            onDismissRequest = { showNamingDialog = false },
            title = { Text(stringResource(R.string.record_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    label = { Text(stringResource(R.string.record_dialog_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startRecording(sessionName.ifBlank { null })
                    showNamingDialog = false
                    sessionName = ""
                }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNamingDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.nav_home),
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isBluetoothEnabled) {
                BluetoothDisabledBanner(
                    onEnableClick = { BluetoothUtil.enableBluetooth(context) }
                )
                Spacer(Modifier.height(16.dp))
            }

            val remoteDevice = state.remoteDevice
            if (remoteDevice == null) {
                EmptyDevicePlaceholder(
                    onScanClick = toDevicesPage,
                    modifier = Modifier.padding(vertical = 48.dp)
                )
            } else {
                HeartbeatCard(
                    sensorName = remoteDevice.getDisplayName(context),
                    macAddress = remoteDevice.getDisplayAddress(context),
                    state = globalState,
                    onConnectClick = { onConnectClick(remoteDevice) },
                    onDisconnectClick = onDisconnectClick
                )

                Spacer(Modifier.height(24.dp))

                SessionControlCard(
                    isRecording = state.isRecording,
                    durationSeconds = state.durationSeconds,
                    currentHeartbeat = state.currentSessionHeartbeat,
                    maxHeartRate = state.maxHeartRate,
                    minHeartRate = state.minHeartRate,
                    recordedRates = state.recordedRates,
                    isConnected = isConnected,
                    onStartClick = { showNamingDialog = true },
                    onStopClick = { viewModel.stopRecording() }
                )
            }

            state.recentSession?.let { session ->
                Spacer(Modifier.height(24.dp))
                RecentSessionSection(session)
            }
        }
    }
}

@Composable
private fun EmptyDevicePlaceholder(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = PulseXIcons.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.height(64.dp).width(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_no_devices),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_scan_devices_desc), // I should check if this string exists
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onScanClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(PulseXIcons.Bluetooth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_scan_devices))
            }
        }
    }
}

@Composable
private fun BluetoothDisabledBanner(
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = PulseXIcons.BluetoothDisabled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.home_bluetooth_off_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onEnableClick) {
                Text(stringResource(R.string.common_enable))
            }
        }
    }
}

@Composable
private fun RecentSessionSection(session: SessionEntity) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateText = remember(session.startTime, locale) {
        SimpleDateFormat("MMMM dd, HH:mm", locale).format(Date(session.startTime))
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_recent_session),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = session.title ?: dateText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDuration((session.endTime - session.startTime) / 1000),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        label = stringResource(R.string.record_max_label),
                        value = stringResource(R.string.heartbeat_value_with_bpm, session.maxHeartRate),
                        color = Color(0xFFE53935)
                    )
                    StatItem(
                        label = stringResource(R.string.record_min_label),
                        value = stringResource(R.string.heartbeat_value_with_bpm, session.minHeartRate),
                        color = Color(0xFF1E88E5)
                    )
                    StatItem(
                        label = stringResource(R.string.history_avg_short),
                        value = stringResource(R.string.heartbeat_value_with_bpm, session.avgHeartRate),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
