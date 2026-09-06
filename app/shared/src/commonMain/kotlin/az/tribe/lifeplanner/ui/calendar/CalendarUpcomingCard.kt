package az.tribe.lifeplanner.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.ui.components.WeekStrip
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.CalendarBlank
import com.adamglin.phosphoricons.bold.CalendarCheck
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home widget for the device calendar (read-only import). When calendar permission is missing it
 * shows a compact connect prompt; once granted it shows a day-selectable week strip and the events
 * for the chosen day (defaults to today). Renders nothing when the platform has no calendar.
 * Backed by [CalendarViewModel] / CalendarReader.
 */
@Composable
fun CalendarUpcomingCard(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val permission = rememberCalendarPermission()
    val upcoming by viewModel.events.collectAsStateWithLifecycle()
    val dayEvents by viewModel.dayEvents.collectAsStateWithLifecycle()

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var selectedEpochDay by rememberSaveable { mutableStateOf(today.toEpochDays()) }
    val selectedDate = remember(selectedEpochDay) { LocalDate.fromEpochDays(selectedEpochDay) }

    LaunchedEffect(permission.state) {
        if (permission.state == CalendarPermissionState.GRANTED) {
            viewModel.load(14) // powers the flag dots on the strip for the coming days
        }
    }
    LaunchedEffect(permission.state, selectedDate) {
        if (permission.state == CalendarPermissionState.GRANTED) {
            viewModel.loadDay(selectedDate)
        }
    }

    when (permission.state) {
        CalendarPermissionState.NOT_AVAILABLE -> Unit
        CalendarPermissionState.GRANTED -> DayCalendarCard(
            selectedDate = selectedDate,
            today = today,
            events = dayEvents,
            flaggedDates = eventDates(upcoming),
            onSelect = { selectedEpochDay = it.toEpochDays() },
            modifier = modifier,
        )
        else -> ConnectCalendarPrompt(onConnect = permission.request, modifier = modifier)
    }
}

/**
 * Compact day-lens section for embedding under the journal hub's own week strip. Reflects the
 * [selectedDate] the hub already tracks; renders nothing unless calendar permission is granted and
 * that day actually has events, so it never clutters an empty day.
 */
@Composable
fun CalendarDayEvents(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val permission = rememberCalendarPermission()
    val dayEvents by viewModel.dayEvents.collectAsStateWithLifecycle()

    LaunchedEffect(permission.state, selectedDate) {
        if (permission.state == CalendarPermissionState.GRANTED) {
            viewModel.loadDay(selectedDate)
        }
    }

    if (permission.state != CalendarPermissionState.GRANTED || dayEvents.isEmpty()) return

    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(tz)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = PhosphorIcons.Bold.CalendarCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ON YOUR CALENDAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            DayEventList(events = dayEvents, tz = tz, today = today)
        }
    }
}

@Composable
private fun DayCalendarCard(
    selectedDate: LocalDate,
    today: LocalDate,
    events: List<CalendarEvent>,
    flaggedDates: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tz = TimeZone.currentSystemDefault()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = PhosphorIcons.Bold.CalendarCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CALENDAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))
            WeekStrip(
                selectedDate = selectedDate,
                entries = emptyList(),
                onSelect = onSelect,
                flaggedDates = flaggedDates,
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = dayLabel(selectedDate, today),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            if (events.isEmpty()) {
                Text(
                    text = "Nothing scheduled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DayEventList(events = events, tz = tz, today = today)
            }
        }
    }
}

@Composable
private fun DayEventList(events: List<CalendarEvent>, tz: TimeZone, today: LocalDate) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        events.forEach { event -> EventRow(event = event, tz = tz, today = today) }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, tz: TimeZone, today: LocalDate) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.widthIn(min = 64.dp)) {
            Text(
                text = timeLabel(event, tz),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val day = dayLabel(event.startEpochMillis, tz, today)
            if (day != null) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = event.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun ConnectCalendarPrompt(onConnect: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = PhosphorIcons.Bold.CalendarBlank,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Connect your calendar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "See your events by day right here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onConnect) {
                Text("Connect", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Local dates that have at least one event, for flagging days on the week strip. */
private fun eventDates(events: List<CalendarEvent>): Set<LocalDate> {
    val tz = TimeZone.currentSystemDefault()
    return events.mapTo(mutableSetOf()) {
        Instant.fromEpochMilliseconds(it.startEpochMillis).toLocalDateTime(tz).date
    }
}

private fun timeLabel(event: CalendarEvent, tz: TimeZone): String {
    if (event.allDay) return "All day"
    val dt = Instant.fromEpochMilliseconds(event.startEpochMillis).toLocalDateTime(tz)
    val hour12 = when (val h = dt.hour % 12) {
        0 -> 12
        else -> h
    }
    val minute = dt.minute.toString().padStart(2, '0')
    val meridiem = if (dt.hour < 12) "AM" else "PM"
    return "$hour12:$minute $meridiem"
}

/** Label for the selected day header: "Today" / "Tomorrow" / weekday + date. */
private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
    today.plus(-1, DateTimeUnit.DAY) -> "Yesterday"
    else -> {
        val weekday = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        "$weekday, $month ${date.dayOfMonth}"
    }
}

private fun dayLabel(startMillis: Long, tz: TimeZone, today: LocalDate): String? {
    val date = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(tz).date
    return when (date) {
        today -> "Today"
        today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
        else -> date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
}
