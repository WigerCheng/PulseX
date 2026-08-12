package io.wiger.pulsex.core.heartbeat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import io.wiger.pulsex.core.notification.NotificationProvider
import io.wiger.pulsex.core.system.SystemProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    private var bluetoothGatt: BluetoothGatt? = null
    private var isForeground = false

    private val binder = HeartbeatBinder()

    inner class HeartbeatBinder : Binder() {
        fun getService(): HeartbeatService = this@HeartbeatService
    }

    private val baseNotification by lazy {
        NotificationCompat.Builder(this, NotificationProvider.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.drawable.ic_heart)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setShowWhen(false)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        systemProvider.isBluetoothEnabledFlow
            .onEach { isEnabled ->
                if (!isEnabled) {
                    Log.d(TAG, "Bluetooth disabled, disconnecting GATT")
                    close()
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started")
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
            val notification = baseNotification
                .setContentTitle(getString(R.string.notification_heartbeat_title, newValue))
                .setShortCriticalText("$newValue")
                .build()
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
        Log.d(TAG, "Connecting to device: ${device.address}")
        close() // Close previous if any
        bluetoothGatt = device.connectGatt(
            this@HeartbeatService,
            false,
            bluetoothGattCallback
        )
        true
    } catch (e: Exception) {
        Log.e(TAG, "connect failed", e)
        false
    }

    @SuppressLint("MissingPermission")
    fun close() {
        Log.d(TAG, "Closing GATT connection")
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
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server")
                gatt?.discoverServices()
                lifecycleScope.launch {
                    broadcastConnectionState(true)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server")
                lifecycleScope.launch {
                    close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                Log.d(TAG, "Services discovered")
                val heartbeatGattService = gatt.getService(BluetoothConstant.heartbeatUUID)
                val heartbeatCharacteristic = heartbeatGattService?.characteristics.orEmpty()
                    .firstOrNull { it.uuid == BluetoothConstant.heartbeatMeasurementUUID }
                
                heartbeatCharacteristic?.let { 
                    enableHeartbeatNotification(gatt, it)
                }
            } else {
                Log.w(TAG, "onServicesDiscovered: status=$status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            processHeartbeatCharacteristic(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                processHeartbeatCharacteristic(characteristic)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableHeartbeatNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        gatt.readCharacteristic(characteristic)
        
        // Start foreground service when we actually start getting data or are about to
        lifecycleScope.launch {
            if (!isForeground) {
                startForegroundCompat()
            }
        }
    }

    private fun processHeartbeatCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.uuid != BluetoothConstant.heartbeatMeasurementUUID) return
        val flag = characteristic.properties
        val format = when (flag and 0x01) {
            0x01 -> BluetoothGattCharacteristic.FORMAT_UINT16
            else -> BluetoothGattCharacteristic.FORMAT_UINT8
        }
        val heartbeat = characteristic.getIntValue(format, 1)
        Log.d(TAG, "Heartbeat received: $heartbeat")
        currentHeartbeat = heartbeat
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
            this,
            NOTIFICATION_ID,
            notification,
            foregroundServiceType
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
