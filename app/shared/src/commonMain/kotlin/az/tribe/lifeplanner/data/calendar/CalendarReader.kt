@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.DeviceCalendar

/**
 * Reads events from the device calendar. One-way (import only) for now.
 * Android: CalendarContract via ContentResolver. iOS: EventKit via EKEventStore.
 * The caller is responsible for holding calendar permission (see [rememberCalendarPermission]);
 * without it, [readUpcomingEvents] returns an empty list rather than throwing.
 */
expect class CalendarReader() {
    /** Whether the platform exposes a calendar provider at all. */
    suspend fun isAvailable(): Boolean

    /**
     * Every calendar the device exposes (one per mail account, plus shared/holiday/birthday ones).
     * Empty if permission is missing. Used by Calendar settings so the user can see exactly which
     * accounts we read from and switch individual calendars off.
     */
    suspend fun listCalendars(): List<DeviceCalendar>

    /** Events from now through [days] ahead, sorted by start time. Empty if permission is missing. */
    suspend fun readUpcomingEvents(days: Int = 7): List<CalendarEvent>

    /**
     * Events overlapping the window [startEpochMillis, endEpochMillis), sorted by start time.
     * Used for day-scoped views (e.g. "what's on my calendar for this day"). Empty if permission
     * is missing. Recurring events are expanded into concrete occurrences within the window.
     */
    suspend fun readEvents(startEpochMillis: Long, endEpochMillis: Long): List<CalendarEvent>
}
