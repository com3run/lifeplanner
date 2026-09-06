package az.tribe.lifeplanner.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.sourceLabel
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.Info
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_refresh

private const val PREVIEW_DAYS = 7

/**
 * "Calendar" integration detail screen. Answers the three things the Integrations row can't:
 * which accounts/calendars we read from, what we actually see in them, and how to turn individual
 * calendars off. Import stays read-only — we never create, edit, or delete device events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val permission = rememberCalendarPermission()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(permission.state) {
        if (permission.state == CalendarPermissionState.GRANTED) {
            viewModel.loadCalendars()
            viewModel.load(PREVIEW_DAYS)
        }
    }

    val enabledCount = calendars.count { it.enabled }
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(tz) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text(
                            "Calendar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (permission.state == CalendarPermissionState.GRANTED) {
                            Text(
                                "$enabledCount of ${calendars.size} calendars • " +
                                    "${events.size} ${if (events.size == 1) "event" else "events"} this week",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back))
                    }
                },
                actions = {
                    if (permission.state == CalendarPermissionState.GRANTED) {
                        IconButton(onClick = {
                            viewModel.loadCalendars()
                            viewModel.load(PREVIEW_DAYS)
                        }) {
                            Icon(
                                PhosphorIcons.Regular.ArrowsClockwise,
                                contentDescription = stringResource(Res.string.cd_refresh),
                                tint = if (isLoading) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (permission.state != CalendarPermissionState.GRANTED) {
            CalendarNotConnected(
                state = permission.state,
                onConnect = permission.request,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            item { ReadOnlyNotice() }

            item { SectionLabel("Calendars we read") }

            if (calendars.isEmpty()) {
                item {
                    EmptyNote(
                        "No calendars found on this device. Add an account in your phone's Calendar " +
                            "app and pull to refresh."
                    )
                }
            }

            // Grouped by account so "which of my mail accounts is this?" is answerable at a glance.
            calendars.groupBy { it.calendar.sourceLabel }.forEach { (account, group) ->
                item(key = "account-$account") { AccountHeader(account, group.count { it.enabled }, group.size) }
                items(group, key = { "cal-${it.calendar.id}" }) { selection ->
                    CalendarToggleRow(
                        selection = selection,
                        onToggle = { enabled ->
                            viewModel.setCalendarEnabled(selection.calendar.id, enabled)
                        },
                    )
                }
            }

            if (calendars.any { !it.enabled }) {
                item {
                    TextButton(onClick = { viewModel.enableAllCalendars() }) {
                        Text("Turn all calendars back on")
                    }
                }
            }

            item { SectionLabel("Next $PREVIEW_DAYS days") }

            if (events.isEmpty()) {
                item {
                    EmptyNote(
                        if (enabledCount == 0) "Every calendar is switched off, so nothing is imported."
                        else "Nothing scheduled in the calendars you've enabled."
                    )
                }
            } else {
                items(events, key = { it.id }) { event ->
                    UpcomingEventRow(event = event, tz = tz, today = today)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ReadOnlyNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.standard),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                PhosphorIcons.Regular.Eye,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Read-only",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "LifePlanner reads events from the calendars below to plan around them. " +
                        "It never creates, edits, or deletes anything in your calendar, and events " +
                        "stay on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AccountHeader(account: String, enabled: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = account,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$enabled/$total on",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CalendarToggleRow(
    selection: CalendarSelection,
    onToggle: (Boolean) -> Unit,
) {
    val calendar = selection.calendar
    // Force full alpha: some providers hand back a calendar color with a zero alpha byte, which
    // would render the dot invisible.
    val dotColor = calendar.colorArgb?.let { Color(it.toInt()).copy(alpha = 1f) }
        ?: MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = LifePlannerDesign.Padding.standard,
                vertical = 10.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (selection.enabled) dotColor else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = calendar.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val tags = buildList {
                    if (calendar.isPrimary) add("Primary")
                    if (calendar.isReadOnly) add("View only")
                }
                if (tags.isNotEmpty()) {
                    Text(
                        text = tags.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(checked = selection.enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun UpcomingEventRow(event: CalendarEvent, tz: TimeZone, today: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.widthIn(min = 76.dp)) {
            Text(
                text = timeLabelFor(event, tz),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = dayLabelFor(event.startEpochMillis, tz, today),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = listOfNotNull(event.calendarName, event.location).joinToString(" • ")
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            PhosphorIcons.Regular.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CalendarNotConnected(
    state: CalendarPermissionState,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(LifePlannerDesign.Padding.screenHorizontal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    RoundedCornerShape(20.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                PhosphorIcons.Regular.CalendarBlank,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (state == CalendarPermissionState.NOT_AVAILABLE) "No calendar on this device"
            else "Calendar not connected",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (state == CalendarPermissionState.NOT_AVAILABLE)
                "This device doesn't expose a calendar we can read."
            else "Grant calendar access to see which accounts and calendars LifePlanner reads, " +
                "and to plan around your events.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state != CalendarPermissionState.NOT_AVAILABLE) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onConnect) { Text("Connect calendar") }
        }
    }
}

private fun timeLabelFor(event: CalendarEvent, tz: TimeZone): String {
    if (event.allDay) return "All day"
    val dt = Instant.fromEpochMilliseconds(event.startEpochMillis).toLocalDateTime(tz)
    val hour12 = when (val h = dt.hour % 12) {
        0 -> 12
        else -> h
    }
    return "$hour12:${dt.minute.toString().padStart(2, '0')} ${if (dt.hour < 12) "AM" else "PM"}"
}

private fun dayLabelFor(epochMillis: Long, tz: TimeZone, today: LocalDate): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(tz).date
    return when (date) {
        today -> "Today"
        today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
        else -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    }
}
