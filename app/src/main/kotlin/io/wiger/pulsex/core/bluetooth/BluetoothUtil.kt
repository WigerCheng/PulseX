package io.wiger.pulsex.core.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.wiger.pulsex.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object BluetoothUtil {

    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getBluetoothAdapter(context: Context): BluetoothAdapter? =
        context.getSystemService<BluetoothManager>()?.adapter

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableBluetooth(context: Context) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        context.startActivity(enableBtIntent)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scanDevicesFlow(
        context: Context,
        scanFilters: List<ScanFilter> = emptyList(),
        scanSettings: ScanSettings = ScanSettings.Builder().build(),
    ): Flow<ScanResult> = callbackFlow {
        val bluetoothLeScanner =
            getBluetoothAdapter(context)?.bluetoothLeScanner ?: return@callbackFlow
        val leScanCallback: ScanCallback =
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    trySend(result)
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    close()
                }
            }
        bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback)
        awaitClose {
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

}

fun BluetoothDevice?.getDisplayName(context: Context): String {
    val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            this?.name
        } else null
    } else {
        this?.name
    }
    
    val displayName = name?.ifBlank {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    this?.alias
                } else null
            } else this?.alias
        } else null
    } ?: context.getString(R.string.unknown_name)
    return displayName
}

fun BluetoothDevice?.getDisplayAddress(context: Context): String {
    return this?.address?.takeIf(String::isNotBlank) ?: context.getString(R.string.unknown_address)
}

fun BluetoothAdapter?.getRemoteLeDevice(address: String): BluetoothDevice? {
    if (!BluetoothAdapter.checkBluetoothAddress(address)) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getRemoteLeDevice(address, BluetoothDevice.ADDRESS_TYPE_PUBLIC)
    } else {
        this?.getRemoteDevice(address)
    }
}

val BluetoothAdapter?.isBluetoothEnabled: Boolean
    get() = this?.isEnabled == true
