package io.wiger.pulsex.core.heartbeat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattConnectionSettings
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.wiger.pulsex.R
import io.wiger.pulsex.core.bluetooth.BluetoothConstant
import io.wiger.pulsex.core.bluetooth.BluetoothLogger
import io.wiger.pulsex.core.notification.NotificationProvider
import io.wiger.pulsex.core.system.SystemProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class HeartbeatService : LifecycleService() {

    companion object {
        private const val TAG = "HeartbeatService"
        private const val NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var systemProvider: SystemProvider

    @Inject
    lateinit var notificationProvider: NotificationProvider

    @Inject
    lateinit var bluetoothLogger: BluetoothLogger

    private var bluetoothGatt: BluetoothGatt? = null
    private var isForeground = false

    private val binder = HeartbeatBinder()

    inner class HeartbeatBinder : Binder() {
        fun getService(): HeartbeatService = this@HeartbeatService
    }

    private val baseNotification by lazy {
        NotificationCompat.Builder(this, NotificationProvider.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name)).setSmallIcon(R.drawable.ic_heart)
            .setOngoing(true).setRequestPromotedOngoing(true).setShowWhen(false)
    }

    override fun onCreate() {
        super.onCreate()
        bluetoothLogger.d(TAG, "Service created")
        systemProvider.isBluetoothEnabledFlow.onEach { isEnabled ->
            if (!isEnabled) {
                bluetoothLogger.w(TAG, "Bluetooth disabled, disconnecting GATT")
                close()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        bluetoothLogger.d(TAG, "Service started")
        return START_STICKY
    }

    private var currentHeartbeat: Int? by Delegates.observable(null) { _, o, n ->
        if (o == n) return@observable
        n?.let(::handleHeartbeatChange)
    }

    private fun handleHeartbeatChange(newValue: Int) {
        // Send via Broadcast as requested
        HeartbeatBroadcastReceiver.sendHeartbeat(this, newValue)

        // Update notification if in foreground
        if (isForeground) {
            val notification = baseNotification.setContentTitle(
                getString(
                    R.string.notification_heartbeat_title, newValue
                )
            ).setShortCriticalText("$newValue").build()
            notificationProvider.notifyNotification(NOTIFICATION_ID, notification)
        }
    }

    private fun broadcastConnectionState(isConnected: Boolean) {
        // We could also have a connection state broadcast, but for now HeartbeatProvider is updated in callback
        HeartbeatProvider.updateConnectionState(isConnected)
        if (!isConnected && isForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForeground = false
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean = try {
        bluetoothLogger.i(TAG, "Connecting to device: ${device.address} (${device.name})")
        close()
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            val setting = BluetoothGattConnectionSettings.Builder().build()
            device.connectGatt(
                setting, Executors.newSingleThreadExecutor(), bluetoothGattCallback
            )
        } else {
            device.connectGatt(
                this@HeartbeatService, false, bluetoothGattCallback
            )
        }
        true
    } catch (e: Exception) {
        bluetoothLogger.e(TAG, "connect failed", e)
        false
    }

    @SuppressLint("MissingPermission")
    fun close() {
        bluetoothLogger.d(TAG, "Closing GATT connection")
        bluetoothGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            bluetoothGatt = null
        }
        broadcastConnectionState(false)
        currentHeartbeat = null
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            bluetoothLogger.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothLogger.i(TAG, "Connected to GATT server")
                gatt?.discoverServices()
                broadcastConnectionState(true)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothLogger.i(TAG, "Disconnected from GATT server")
                close()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                bluetoothLogger.w(TAG, "status != BluetoothGatt.GATT_SUCCESS: status=$status")
                return
            }
            if (gatt == null) {
                bluetoothLogger.w(TAG, "gatt == null")
                return
            }
            val heartbeatGattService: BluetoothGattService? =
                gatt.getService(BluetoothConstant.heartbeatServiceUUID)
            if (heartbeatGattService == null) {
                bluetoothLogger.w(TAG, "Heartbeat service not found")
                return
            }
            val heartbeatCharacteristic: BluetoothGattCharacteristic? =
                heartbeatGattService.getCharacteristic(BluetoothConstant.heartbeatMeasurementUUID)
            if (heartbeatCharacteristic == null) {
                bluetoothLogger.w(TAG, "Heartbeat characteristic not found")
                return
            }
            bluetoothLogger.i(TAG, "Heartbeat characteristic found")
            enableNotifications(gatt, heartbeatCharacteristic)

            lifecycleScope.launch {
                if (!isForeground) {
                    startForegroundCompat()
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun enableNotifications(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            val properties = characteristic.properties
            val isNotifySupported =
                (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
            if (!isNotifySupported) {
                bluetoothLogger.w(TAG, "Characteristic does not support notifications")
                gatt.readCharacteristic(characteristic)
                return
            }
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor =
                characteristic.getDescriptor(BluetoothConstant.clientCharacteristicConfigUUID)
            if (descriptor == null) {
                bluetoothLogger.w(TAG, "Client characteristic configuration descriptor not found")
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gatt.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                processHeartbeatData(value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
        ) {
            processHeartbeatData(value)
        }
    }

    private fun processHeartbeatData(value: ByteArray) {
        if (value.isEmpty()) {
            bluetoothLogger.w(TAG, "Heartbeat data is empty")
            return
        }
        val heartbeatData = parseHeartRateMeasurement(value)
        bluetoothLogger.d(TAG, "Heartbeat data: $heartbeatData")
        currentHeartbeat = heartbeatData.heartRate
    }

    private fun startForegroundCompat() {
        Log.d(TAG, "Starting foreground service")
        val notification = baseNotification.build()
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, notification, foregroundServiceType
        )
        isForeground = true
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        close()
        super.onDestroy()
    }
}
