package az.tribe.lifeplanner.notification

import co.touchlab.kermit.Logger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual fun notifyNow(id: String, title: String, message: String) {
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(message)
        setSound(UNNotificationSound.defaultSound())
    }
    // A nil trigger delivers the notification immediately.
    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = id,
        content = content,
        trigger = null
    )
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
        if (error != null) {
            Logger.e("LocalNotifier") { "notifyNow failed: ${error.localizedDescription}" }
        }
    }
}
