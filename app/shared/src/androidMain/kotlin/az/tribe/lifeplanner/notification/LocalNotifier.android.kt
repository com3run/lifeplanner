package az.tribe.lifeplanner.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import az.tribe.lifeplanner.MainActivity
import az.tribe.lifeplanner.shared.R
import co.touchlab.kermit.Logger

private const val CHANNEL_ID = "reminders"
private const val CHANNEL_NAME = "Reminders"

actual fun notifyNow(id: String, title: String, message: String) {
    val context = AndroidNotificationScheduler.appContext ?: run {
        Logger.w("LocalNotifier") { "notifyNow skipped, no app context" }
        return
    }
    try {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val tapIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        notificationManager.notify(id.hashCode(), notification)
    } catch (e: Exception) {
        Logger.e("LocalNotifier") { "notifyNow failed: ${e.message}" }
    }
}
