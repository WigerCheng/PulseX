package io.wiger.pulsex.features.scan

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
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
import io.wiger.pulsex.data.local.db.ScanResultEntity
import io.wiger.pulsex.ui.PulseXIcons

@SuppressLint("MissingPermission")
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    onDeviceSelected: () -> Unit,
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
                        stringResource(R.string.scan_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    if (uiState.isScanning) {
                        FilledIconButton({ viewModel.onIntent(ScanIntent.StopScanning) }) {
                            Icon(PulseXIcons.Stop, stringResource(R.string.cd_stop))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!uiState.isBluetoothEnabled) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = PulseXIcons.BluetoothDisabled,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.home_bluetooth_off_warning),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { BluetoothUtil.enableBluetooth(context) }) {
                            Text(stringResource(R.string.common_enable))
                        }
                    }
                }
            } else {
                if (uiState.isScanning) {
                    LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearWavyProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth())
                }
                ScanDevicesContent(
                    uiState = uiState,
                    onIntent = viewModel::onIntent,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScanDevicesContent(
    uiState: ScanUiState,
    onIntent: (ScanIntent) -> Unit,
    onDeviceSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.results.isEmpty() && !uiState.isScanning) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.scan_no_devices), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.size(16.dp))
                Button(onClick = { onIntent(ScanIntent.StartScanning) }) {
                    Text(stringResource(R.string.scan_start))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.results,
                    key = { it.address },
                    contentType = { "DeviceResult" }
                ) { result ->
                    val isSelected = result.address == uiState.selectedDeviceAddress
                    DeviceResult(
                        scanResult = result,
                        isSelected = isSelected,
                        modifier = Modifier.animateItem(),
                        onDeviceClick = {
                            onIntent(ScanIntent.SelectDevice(result.address, result.name))
                            onDeviceSelected()
                        }
                    )
                }
                item {
                    if (uiState.results.isNotEmpty() && !uiState.isScanning) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { onIntent(ScanIntent.StartScanning) }
                            ) {
                                Text(stringResource(R.string.scan_rescan))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceResult(
    scanResult: ScanResultEntity,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onDeviceClick: () -> Unit,
) {
    Card(
        onClick = onDeviceClick,
        modifier = modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = scanResult.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = scanResult.address,
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}
