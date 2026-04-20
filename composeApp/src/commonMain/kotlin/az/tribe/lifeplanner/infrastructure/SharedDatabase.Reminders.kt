package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.ReminderEntity
import az.tribe.lifeplanner.database.ReminderSettingsEntity
import az.tribe.lifeplanner.database.ScheduledNotificationEntity
import az.tribe.lifeplanner.database.UserActivityPatternEntity

// --- Reminder operations ---

suspend fun SharedDatabase.getAllReminders(): List<ReminderEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllReminders().executeAsList() }
}

suspend fun SharedDatabase.getEnabledReminders(): List<ReminderEntity> {
    return this { db -> db.lifePlannerDBQueries.getEnabledReminders().executeAsList() }
}

suspend fun SharedDatabase.getReminderById(id: String): ReminderEntity? {
    return this { db -> db.lifePlannerDBQueries.getReminderById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getRemindersByGoalId(goalId: String): List<ReminderEntity> {
    return this { db -> db.lifePlannerDBQueries.getRemindersByGoalId(goalId).executeAsList() }
}

suspend fun SharedDatabase.getRemindersByHabitId(habitId: String): List<ReminderEntity> {
    return this { db -> db.lifePlannerDBQueries.getRemindersByHabitId(habitId).executeAsList() }
}

suspend fun SharedDatabase.getRemindersByType(type: String): List<ReminderEntity> {
    return this { db -> db.lifePlannerDBQueries.getRemindersByType(type).executeAsList() }
}

suspend fun SharedDatabase.insertReminder(
    id: String,
    title: String,
    message: String,
    type: String,
    frequency: String,
    scheduledTime: String,
    scheduledDays: String,
    linkedGoalId: String?,
    linkedHabitId: String?,
    isEnabled: Long,
    isSmartTiming: Long,
    lastTriggeredAt: String?,
    snoozedUntil: String?,
    createdAt: String,
    updatedAt: String?
) {
    this { db ->
        db.lifePlannerDBQueries.insertReminder(
            id, title, message, type, frequency, scheduledTime, scheduledDays,
            linkedGoalId, linkedHabitId, isEnabled, isSmartTiming, lastTriggeredAt,
            snoozedUntil, createdAt, updatedAt,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.updateReminder(
    id: String,
    title: String,
    message: String,
    type: String,
    frequency: String,
    scheduledTime: String,
    scheduledDays: String,
    linkedGoalId: String?,
    linkedHabitId: String?,
    isEnabled: Long,
    isSmartTiming: Long,
    updatedAt: String?
) {
    this { db ->
        db.lifePlannerDBQueries.updateReminder(
            title, message, type, frequency, scheduledTime, scheduledDays,
            linkedGoalId, linkedHabitId, isEnabled, isSmartTiming, updatedAt, id
        )
    }
}

suspend fun SharedDatabase.updateReminderLastTriggered(id: String, lastTriggeredAt: String) {
    this { db -> db.lifePlannerDBQueries.updateReminderLastTriggered(lastTriggeredAt, id) }
}

suspend fun SharedDatabase.snoozeReminder(id: String, snoozedUntil: String) {
    this { db -> db.lifePlannerDBQueries.snoozeReminder(snoozedUntil, id) }
}

suspend fun SharedDatabase.enableReminder(id: String) {
    this { db -> db.lifePlannerDBQueries.enableReminder(id) }
}

suspend fun SharedDatabase.disableReminder(id: String) {
    this { db -> db.lifePlannerDBQueries.disableReminder(id) }
}

suspend fun SharedDatabase.enableAllReminders() {
    this { db -> db.lifePlannerDBQueries.enableAllReminders() }
}

suspend fun SharedDatabase.disableAllReminders() {
    this { db -> db.lifePlannerDBQueries.disableAllReminders() }
}

suspend fun SharedDatabase.deleteReminder(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteReminder(nowTimestamp(), id) }
}

suspend fun SharedDatabase.getReminderCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getReminderCount().executeAsOne() }
}

// --- Reminder Settings operations ---

suspend fun SharedDatabase.getReminderSettings(): ReminderSettingsEntity? {
    return this { db -> db.lifePlannerDBQueries.getReminderSettings().executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertReminderSettings(
    id: String,
    isEnabled: Long,
    quietHoursStart: String,
    quietHoursEnd: String,
    preferredMorningTime: String,
    preferredEveningTime: String,
    smartTimingEnabled: Long,
    maxRemindersPerDay: Long,
    weeklyReviewDay: String,
    weeklyReviewTime: String
) {
    this { db ->
        db.lifePlannerDBQueries.insertReminderSettings(
            id, isEnabled, quietHoursStart, quietHoursEnd, preferredMorningTime,
            preferredEveningTime, smartTimingEnabled, maxRemindersPerDay,
            weeklyReviewDay, weeklyReviewTime
        )
    }
}

suspend fun SharedDatabase.updateReminderSettings(
    isEnabled: Long,
    quietHoursStart: String,
    quietHoursEnd: String,
    preferredMorningTime: String,
    preferredEveningTime: String,
    smartTimingEnabled: Long,
    maxRemindersPerDay: Long,
    weeklyReviewDay: String,
    weeklyReviewTime: String
) {
    this { db ->
        db.lifePlannerDBQueries.updateReminderSettings(
            isEnabled, quietHoursStart, quietHoursEnd, preferredMorningTime,
            preferredEveningTime, smartTimingEnabled, maxRemindersPerDay,
            weeklyReviewDay, weeklyReviewTime
        )
    }
}

// --- User Activity Pattern operations ---

suspend fun SharedDatabase.getUserActivityPattern(): UserActivityPatternEntity? {
    return this { db -> db.lifePlannerDBQueries.getUserActivityPattern().executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertUserActivityPattern(
    id: String,
    mostActiveHours: String,
    mostActiveDays: String,
    averageResponseTime: Long,
    bestCheckInTimes: String,
    lastUpdated: String
) {
    this { db ->
        db.lifePlannerDBQueries.insertUserActivityPattern(
            id, mostActiveHours, mostActiveDays, averageResponseTime, bestCheckInTimes, lastUpdated
        )
    }
}

suspend fun SharedDatabase.updateUserActivityPattern(
    mostActiveHours: String,
    mostActiveDays: String,
    averageResponseTime: Long,
    bestCheckInTimes: String,
    lastUpdated: String
) {
    this { db ->
        db.lifePlannerDBQueries.updateUserActivityPattern(
            mostActiveHours, mostActiveDays, averageResponseTime, bestCheckInTimes, lastUpdated
        )
    }
}

// --- Scheduled Notification operations ---

suspend fun SharedDatabase.getScheduledNotifications(): List<ScheduledNotificationEntity> {
    return this { db -> db.lifePlannerDBQueries.getScheduledNotifications().executeAsList() }
}

suspend fun SharedDatabase.getScheduledNotificationById(id: String): ScheduledNotificationEntity? {
    return this { db -> db.lifePlannerDBQueries.getScheduledNotificationById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getScheduledNotificationsByReminder(reminderId: String): List<ScheduledNotificationEntity> {
    return this { db -> db.lifePlannerDBQueries.getScheduledNotificationsByReminder(reminderId).executeAsList() }
}

suspend fun SharedDatabase.insertScheduledNotification(
    id: String,
    reminderId: String,
    title: String,
    message: String,
    scheduledAt: String,
    isDelivered: Long,
    deliveredAt: String?,
    isSnoozed: Long,
    isDismissed: Long
) {
    this { db ->
        db.lifePlannerDBQueries.insertScheduledNotification(
            id, reminderId, title, message, scheduledAt, isDelivered, deliveredAt, isSnoozed, isDismissed
        )
    }
}

suspend fun SharedDatabase.markNotificationDelivered(id: String, deliveredAt: String) {
    this { db -> db.lifePlannerDBQueries.markNotificationDelivered(deliveredAt, id) }
}

suspend fun SharedDatabase.markNotificationSnoozed(id: String) {
    this { db -> db.lifePlannerDBQueries.markNotificationSnoozed(id) }
}

suspend fun SharedDatabase.dismissNotification(id: String) {
    this { db -> db.lifePlannerDBQueries.dismissNotification(id) }
}

suspend fun SharedDatabase.deleteScheduledNotification(id: String) {
    // ScheduledNotificationEntity has no is_deleted column; dismiss instead
    this { db -> db.lifePlannerDBQueries.dismissNotification(id) }
}

suspend fun SharedDatabase.deleteScheduledNotificationsByReminder(reminderId: String) {
    // Dismiss all notifications for the reminder (no soft-delete column on this table)
    this { db ->
        val notifications = db.lifePlannerDBQueries.getScheduledNotificationsByReminder(reminderId).executeAsList()
        notifications.forEach { n -> db.lifePlannerDBQueries.dismissNotification(n.id) }
    }
}

suspend fun SharedDatabase.deleteDeliveredNotifications(beforeDate: String) {
    this { db -> db.lifePlannerDBQueries.deleteDeliveredNotifications(beforeDate) }
}

suspend fun SharedDatabase.getPendingNotificationCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getPendingNotificationCount().executeAsOne() }
}
