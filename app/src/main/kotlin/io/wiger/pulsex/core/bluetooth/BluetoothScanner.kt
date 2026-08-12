package io.wiger.pulsex.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter?
) {

    @SuppressLint("MissingPermission")
    fun scanDevices(
        scanFilters: List<ScanFilter> = emptyList(),
        scanSettings: ScanSettings = ScanSettings.Builder().build(),
    ): Flow<ScanResult> = callbackFlow {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return@callbackFlow
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
