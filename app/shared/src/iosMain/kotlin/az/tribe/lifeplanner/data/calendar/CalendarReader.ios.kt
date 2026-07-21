@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.data.calendar

import az.tribe.lifeplanner.domain.model.CalendarEvent
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusFullAccess
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual class CalendarReader {

    private val store = EKEventStore()

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun readUpcomingEvents(days: Int): List<CalendarEvent> {
        val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
        val granted = status == EKAuthorizationStatusAuthorized || status == EKAuthorizationStatusFullAccess
        if (!granted) return emptyList()

        val start = NSDate()
        val end = start.dateByAddingTimeInterval(days.toDouble() * 24.0 * 60.0 * 60.0)
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
            )
        }.sortedBy { it.startEpochMillis }
    }
}
