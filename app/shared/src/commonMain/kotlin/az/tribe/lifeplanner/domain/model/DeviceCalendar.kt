package az.tribe.lifeplanner.domain.model

/**
 * One calendar exposed by the device (Android CalendarContract.Calendars / iOS EventKit EKCalendar).
 *
 * A device typically exposes several: the primary Google/iCloud calendar for each signed-in mail
 * account, plus shared, birthday, and holiday calendars. We read events from all of them by
 * default; the user can turn individual ones off in Calendar settings (see `CalendarPreferences`).
 */
data class DeviceCalendar(
    val id: String,
    val displayName: String,
    /** The account the calendar belongs to, usually an email address (e.g. `you@gmail.com`). */
    val accountName: String? = null,
    /** Raw platform account type (`com.google`, `LOCAL`, iOS `EKSourceType` title). */
    val accountType: String? = null,
    val colorArgb: Long? = null,
    val isPrimary: Boolean = false,
    /** True when the platform only grants read access; we import read-only regardless. */
    val isReadOnly: Boolean = true,
)

/**
 * Human label for the account a calendar came from — this is the "which mail account is this?"
 * answer shown in Calendar settings. Falls back to a friendly name for the platform account type
 * when there is no address (local/subscribed calendars).
 */
val DeviceCalendar.sourceLabel: String
    get() {
        accountName?.takeIf { it.isNotBlank() && it != displayName }?.let { return it }
        return when (accountType?.lowercase()) {
            null, "" -> "This device"
            "local" -> "On this device"
            "com.google" -> "Google"
            "com.google.android.gm.exchange", "com.android.exchange" -> "Exchange"
            "com.apple.calendar", "caldav" -> "iCloud"
            "subscribed" -> "Subscribed"
            "birthdays" -> "Birthdays"
            else -> accountType ?: "This device"
        }
    }
