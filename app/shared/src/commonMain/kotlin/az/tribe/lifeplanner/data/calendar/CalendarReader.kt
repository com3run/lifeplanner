@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import az.tribe.lifeplanner.domain.model.CalendarEvent

/**
 * Reads events from the device calendar. One-way (import only) for now.
 * Android: CalendarContract via ContentResolver. iOS: EventKit via EKEventStore.
 * The caller is responsible for holding calendar permission (see [rememberCalendarPermission]);
 * without it, [readUpcomingEvents] returns an empty list rather than throwing.
 */
expect class CalendarReader() {
    /** Whether the platform exposes a calendar provider at all. */
    suspend fun isAvailable(): Boolean

    /** Events from now through [days] ahead, sorted by start time. Empty if permission is missing. */
    suspend fun readUpcomingEvents(days: Int = 7): List<CalendarEvent>
}
