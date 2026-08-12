package io.wiger.pulsex.core.system

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import io.wiger.pulsex.core.bluetooth.BluetoothBroadcastReceiver
import io.wiger.pulsex.core.bluetooth.BluetoothUtil
import io.wiger.pulsex.core.heartbeat.HeartbeatProvider
import io.wiger.pulsex.core.notification.NotificationProvider
import io.wiger.pulsex.data.local.pref.AppPreference
import io.wiger.pulsex.data.local.pref.PulseXState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class SystemProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val notificationProvider: NotificationProvider,
    appPreference: DataStore<AppPreference>,
) : DefaultLifecycleObserver {

    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        refresh()
    }

    fun refresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    val isBluetoothEnabledFlow: Flow<Boolean> = callbackFlow {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val receiver = BluetoothBroadcastReceiver { _, new ->
            val isEnable = new == BluetoothAdapter.STATE_ON
            trySend(isEnable)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.onStart {
        val isBluetoothEnabled = bluetoothAdapter?.isEnabled ?: false
        emit(isBluetoothEnabled)
    }.distinctUntilChanged()

    val hasBluetoothPermission: Flow<Boolean> = _refreshTrigger.map {
        BluetoothUtil.hasBluetoothPermission(context)
    }.distinctUntilChanged()

    val hasNotificationPermission: Flow<Boolean> = _refreshTrigger.map {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }.distinctUntilChanged()

    val hasLiveUpdatePermission: Flow<Boolean> = _refreshTrigger.map {
        notificationProvider.canPostPromotedNotifications()
    }.distinctUntilChanged()

    private val heartbeat: Flow<Int> = io.wiger.pulsex.core.heartbeat.heartbeatFlow(context)

    val pulseXState: Flow<PulseXState> = combine(
        isBluetoothEnabledFlow,
        hasBluetoothPermission,
        HeartbeatProvider.isConnected,
        heartbeat,
        appPreference.data
    ) { isBtEnabled, hasBtPerm, isConnected, heartbeatValue, pref ->
        when {
            !isBtEnabled || !hasBtPerm -> PulseXState.BluetoothError(isBtEnabled)
            !isConnected -> PulseXState.Disconnected(
                deviceName = pref.deviceName,
                deviceAddress = pref.deviceAddress
            )

            else -> PulseXState.Connected(
                heartbeat = heartbeatValue,
                deviceName = pref.deviceName,
                deviceAddress = pref.deviceAddress
            )
        }
    }.distinctUntilChanged()
}
