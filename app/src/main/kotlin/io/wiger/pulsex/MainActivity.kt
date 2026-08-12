package io.wiger.pulsex

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.wiger.pulsex.core.heartbeat.HeartbeatService
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    private var heartbeatService by mutableStateOf<HeartbeatService?>(null)
    private var isBound by mutableStateOf(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as HeartbeatService.HeartbeatBinder
            heartbeatService = binder.getService()
            isBound = true
            appViewModel.remoteDevice.value?.let { heartbeatService?.connect(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            heartbeatService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        startService(Intent(this, HeartbeatService::class.java))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.remoteDevice.collect { device ->
                    if (device != null && !isBound) {
                        bindService(
                            Intent(this@MainActivity, HeartbeatService::class.java),
                            connection,
                            Context.BIND_AUTO_CREATE
                        )
                    }
                }
            }
        }

        setContent {
            val isOnboardingCompleted by appViewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
            App(
                isOnboardingCompleted = isOnboardingCompleted,
                onConnectClick = { device ->
                    heartbeatService?.connect(device)
                },
                onDisconnectClick = {
                    heartbeatService?.close()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
