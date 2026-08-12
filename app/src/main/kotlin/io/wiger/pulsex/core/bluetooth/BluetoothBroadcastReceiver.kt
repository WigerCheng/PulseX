package io.wiger.pulsex.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Stable

class BluetoothBroadcastReceiver(
    private var bluetoothStateChangeListener: BluetoothStateChangeListener? = null,
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val extra =
            intent?.takeIf { it.action == BluetoothAdapter.ACTION_STATE_CHANGED }?.extras ?: return
        val state = extra.getInt(BluetoothAdapter.EXTRA_STATE)
        val oldState = extra.getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE)
        bluetoothStateChangeListener?.onChange(oldState, state)
    }
}

@Stable
fun interface BluetoothStateChangeListener {
    fun onChange(oldState: Int, newState: Int)
}
