package io.wiger.pulsex.core.heartbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Stable
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class HeartbeatBroadcastReceiver : BroadcastReceiver() {

    var heartbeatBroadcastListener: HeartbeatBroadcastListener? = null

    companion object {
        const val ACTION_HEARTBEAT = "io.wiger.pulsex.action.heartbeat"
        private const val EXTRA_HEARTBEAT = "extra_heartbeat"

        @JvmStatic
        fun sendHeartbeat(context: Context, heartbeat: Int) {
            val intent = Intent().apply {
                setPackage(context.packageName)
                action = ACTION_HEARTBEAT
                putExtra(EXTRA_HEARTBEAT, heartbeat)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.takeIf { it.action == ACTION_HEARTBEAT }?.extras ?: return
        val heartbeat = extras.getInt(EXTRA_HEARTBEAT)
        heartbeatBroadcastListener?.onHeartbeatChange(heartbeat)
        HeartbeatProvider.updateHeartbeat(heartbeat)
    }
}

@Stable
fun interface HeartbeatBroadcastListener {
    fun onHeartbeatChange(heartbeat: Int)
}

fun heartbeatFlow(context: Context): Flow<Int> = callbackFlow {
    val intentFilter = IntentFilter(HeartbeatBroadcastReceiver.ACTION_HEARTBEAT)
    val receiver = HeartbeatBroadcastReceiver().apply {
        heartbeatBroadcastListener = HeartbeatBroadcastListener { heartbeat ->
            trySend(heartbeat)
        }
    }
    ContextCompat.registerReceiver(
        context,
        receiver,
        intentFilter,
        ContextCompat.RECEIVER_NOT_EXPORTED
    )
    awaitClose {
        context.unregisterReceiver(receiver)
    }
}.onStart { emit(0) }
