@file:OptIn(ExperimentalPermissionsApi::class)

package io.wiger.pulsex

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.wiger.pulsex.features.main.MainScreen
import io.wiger.pulsex.features.onboarding.OnboardingScreen
import io.wiger.pulsex.ui.theme.PulseXTheme

@Composable
fun App(
    isOnboardingCompleted: Boolean,
    onConnectClick: (BluetoothDevice) -> Unit = {},
    onDisconnectClick: () -> Unit = {},
) {
    PulseXTheme {
        val navController = rememberNavController()

        val startDes = if (isOnboardingCompleted) AppNavigation.Main else AppNavigation.Onboarding

        NavHost(navController, startDes, modifier = Modifier.fillMaxSize()) {
            composable<AppNavigation.Onboarding> {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(AppNavigation.Main) {
                            popUpTo<AppNavigation.Onboarding> { inclusive = true }
                        }
                    }
                )
            }
            composable<AppNavigation.Main> {
                MainScreen(
                    onConnectClick = onConnectClick,
                    onDisconnectClick = onDisconnectClick
                )
            }
        }
    }
}
