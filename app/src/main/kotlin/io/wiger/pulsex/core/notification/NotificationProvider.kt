package io.wiger.pulsex.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationProvider @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        const val CHANNEL_ID = "HeartbeatChannel"
        private const val CHANNEL_NAME = "HeartbeatChannel"

        @JvmStatic
        fun startPromotionNotificationSetting(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(intent)
        }
    }

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val serviceChannel: NotificationChannelCompat = with(
            NotificationChannelCompat.Builder(
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        ) {
            setName(CHANNEL_NAME)
            build()
        }
        notificationManager.createNotificationChannel(serviceChannel)
    }

    fun notifyNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun canPostPromotedNotifications(): Boolean = notificationManager.canPostPromotedNotifications()

}
