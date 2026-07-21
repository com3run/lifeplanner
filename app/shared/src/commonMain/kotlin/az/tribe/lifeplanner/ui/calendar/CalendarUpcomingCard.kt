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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.CalendarEvent
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.CalendarBlank
import com.adamglin.phosphoricons.bold.CalendarCheck
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

private const val HOME_EVENT_LIMIT = 4

/**
 * Home widget for the device calendar (read-only import). When calendar permission is missing it
 * shows a compact connect prompt; once granted it loads and lists the next few upcoming events.
 * Renders nothing when the platform has no calendar. Backed by [CalendarViewModel] / CalendarReader.
 */
@Composable
fun CalendarUpcomingCard(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val permission = rememberCalendarPermission()
    val events by viewModel.events.collectAsState()
    val loaded by viewModel.loaded.collectAsState()

    LaunchedEffect(permission.state) {
        if (permission.state == CalendarPermissionState.GRANTED) {
            viewModel.load(7)
        }
    }

    when (permission.state) {
        CalendarPermissionState.NOT_AVAILABLE -> Unit
        CalendarPermissionState.GRANTED -> UpcomingEventsCard(
            events = events,
            loaded = loaded,
            modifier = modifier,
        )
        else -> ConnectCalendarPrompt(onConnect = permission.request, modifier = modifier)
    }
}

@Composable
private fun UpcomingEventsCard(
    events: List<CalendarEvent>,
    loaded: Boolean,
    modifier: Modifier = Modifier,
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    val shown = events.take(HOME_EVENT_LIMIT)

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
                    text = "UPCOMING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (shown.isEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (loaded) "Calendar connected. Nothing scheduled in the next 7 days."
                    else "Calendar connected. Checking your schedule…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(12.dp))
                shown.forEachIndexed { index, event ->
                    EventRow(event = event, tz = tz, today = today)
                    if (index != shown.lastIndex) Spacer(Modifier.height(12.dp))
                }
                if (events.size > shown.size) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "+${events.size - shown.size} more",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
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
                    text = "See your upcoming events right here.",
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

private fun dayLabel(startMillis: Long, tz: TimeZone, today: LocalDate): String? {
    val date = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(tz).date
    return when (date) {
        today -> "Today"
        today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
        else -> date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
}
