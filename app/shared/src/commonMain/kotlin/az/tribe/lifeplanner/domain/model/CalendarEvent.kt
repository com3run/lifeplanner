package az.tribe.lifeplanner.domain.model

/**
 * A single event read from the device calendar (Android CalendarContract / iOS EventKit).
 * Times are epoch milliseconds (UTC) so the model stays platform-neutral; the UI formats them
 * in the user's time zone. Read-only for now; writing events back to the calendar is a later phase.
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val allDay: Boolean,
    val location: String? = null,
    val calendarName: String? = null,
    /** Id of the [DeviceCalendar] this came from, so the user's per-calendar toggles can filter it. */
    val calendarId: String? = null,
)
