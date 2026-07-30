@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.DeviceCalendar
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKCalendar
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSourceType
import platform.Foundation.NSDate
import platform.Foundation.NSTimeIntervalSince1970
import platform.Foundation.dateByAddingTimeInterval
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual class CalendarReader {

    private val store = EKEventStore()

    actual suspend fun isAvailable(): Boolean = true

    private fun isAuthorized(): Boolean {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        return status == EKAuthorizationStatusAuthorized || status == EKAuthorizationStatusFullAccess
    }

    actual suspend fun listCalendars(): List<DeviceCalendar> {
        if (!isAuthorized()) return emptyList()

        @Suppress("UNCHECKED_CAST")
        val calendars = store.calendarsForEntityType(EKEntityType.EKEntityTypeEvent) as? List<EKCalendar>
            ?: return emptyList()

        return calendars.map { calendar ->
            val source = calendar.source
            DeviceCalendar(
                id = calendar.calendarIdentifier,
                displayName = calendar.title.takeIf { it.isNotBlank() } ?: "(unnamed)",
                // EventKit exposes the account as the source title ("iCloud", "Gmail", the address).
                accountName = source?.title?.takeIf { it.isNotBlank() },
                accountType = when (source?.sourceType) {
                    EKSourceType.EKSourceTypeLocal -> "local"
                    EKSourceType.EKSourceTypeExchange -> "com.android.exchange"
                    EKSourceType.EKSourceTypeCalDAV, EKSourceType.EKSourceTypeMobileMe -> "caldav"
                    EKSourceType.EKSourceTypeSubscribed -> "subscribed"
                    EKSourceType.EKSourceTypeBirthdays -> "birthdays"
                    else -> null
                },
                // EKCalendar exposes a CGColor; converting it is not worth the cinterop, so the UI
                // falls back to a theme tint.
                colorArgb = null,
                isPrimary = calendar.calendarIdentifier == store.defaultCalendarForNewEvents?.calendarIdentifier,
                isReadOnly = !calendar.allowsContentModifications,
            )
        }.sortedWith(compareBy({ it.accountName ?: "" }, { it.displayName }))
    }

    actual suspend fun readUpcomingEvents(days: Int): List<CalendarEvent> {
        val start = NSDate()
        val end = start.dateByAddingTimeInterval(days.toDouble() * 24.0 * 60.0 * 60.0)
        return readMatching(start, end)
    }

    actual suspend fun readEvents(startEpochMillis: Long, endEpochMillis: Long): List<CalendarEvent> {
        val start = NSDate(timeIntervalSinceReferenceDate = startEpochMillis / 1000.0 - NSTimeIntervalSince1970)
        val end = NSDate(timeIntervalSinceReferenceDate = endEpochMillis / 1000.0 - NSTimeIntervalSince1970)
        return readMatching(start, end)
    }

    private fun readMatching(start: NSDate, end: NSDate): List<CalendarEvent> {
        if (!isAuthorized()) return emptyList()

        val predicate = store.predicateForEventsWithStartDate(start, endDate = end, calendars = null)

        @Suppress("UNCHECKED_CAST")
        val matches = store.eventsMatchingPredicate(predicate) as? List<EKEvent> ?: return emptyList()

        return matches.mapNotNull { event ->
            val eventStart = event.startDate ?: return@mapNotNull null
            val eventEnd = event.endDate ?: eventStart
            CalendarEvent(
                id = event.eventIdentifier
                    ?: "${event.title.orEmpty()}@${eventStart.timeIntervalSince1970}",
                title = event.title?.takeIf { it.isNotBlank() } ?: "(no title)",
                startEpochMillis = (eventStart.timeIntervalSince1970 * 1000).toLong(),
                endEpochMillis = (eventEnd.timeIntervalSince1970 * 1000).toLong(),
                allDay = event.allDay,
                location = event.location?.takeIf { it.isNotBlank() },
                calendarName = event.calendar?.title,
                calendarId = event.calendar?.calendarIdentifier,
            )
        }.sortedBy { it.startEpochMillis }
    }
}
