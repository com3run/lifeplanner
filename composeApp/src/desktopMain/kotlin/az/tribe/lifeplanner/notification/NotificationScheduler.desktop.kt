package az.tribe.lifeplanner.notification

import az.tribe.lifeplanner.domain.model.Reminder

private object DesktopNotificationScheduler : NotificationSchedulerInterface {
    override fun schedule(reminder: Reminder) {}
    override fun cancel(reminderId: String) {}
    override fun cancelAll() {}
}

actual fun getNotificationScheduler(): NotificationSchedulerInterface = DesktopNotificationScheduler
