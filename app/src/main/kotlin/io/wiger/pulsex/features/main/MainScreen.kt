@file:OptIn(ExperimentalMaterial3Api::class)

package io.wiger.pulsex.features.main

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.wiger.pulsex.R
import io.wiger.pulsex.features.history.HistoryScreen
import io.wiger.pulsex.features.home.HomeScreen
import io.wiger.pulsex.features.scan.ScanScreen
import io.wiger.pulsex.features.settings.SettingScreen
import io.wiger.pulsex.ui.PulseXIcons
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onConnectClick: (BluetoothDevice) -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    toBluetoothLogs: () -> Unit = {}
) {
    val pagerState = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(PulseXIcons.Dashboard, contentDescription = stringResource(R.string.cd_home)) },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(PulseXIcons.History, contentDescription = stringResource(R.string.cd_record)) },
                    label = { Text(stringResource(R.string.nav_record)) }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                    icon = { Icon(PulseXIcons.Bluetooth, contentDescription = stringResource(R.string.cd_search)) },
                    label = { Text(stringResource(R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 3,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    icon = { Icon(PulseXIcons.Settings, contentDescription = stringResource(R.string.cd_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    toDevicesPage = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                    onConnectClick = onConnectClick,
                    onDisconnectClick = onDisconnectClick
                )

                1 -> HistoryScreen()

                2 -> ScanScreen(
                    onDeviceSelected = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )

                3 -> SettingScreen(
                    onLogsClick = toBluetoothLogs
                )
            }
        }
    }
}
