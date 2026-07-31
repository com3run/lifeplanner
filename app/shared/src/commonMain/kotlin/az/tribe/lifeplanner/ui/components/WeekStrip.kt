package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsInSimple
import com.adamglin.phosphoricons.regular.ArrowsOutSimple
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.CaretRight
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * A scrollable "day lens", mood-calendar style: swipe through weeks (two per page on wide screens),
 * or expand to a full month grid with month nav. Each day shows its journal mood emoji (or a birthday
 * cake), a flag dot when a goal/milestone is due, today ringed, and the selected day filled.
 *
 * [engagement] folds the week's output into this same banner instead of a separate "This Week" card
 * below it: collapsed it is one quiet line under the strip, expanded it opens into the full set of
 * counts beside the month. Same numbers, one object on the screen instead of two.
 *
 * [birthdayMonthDay] is (month 1-12, day). [flaggedDates] get a small marker.
 */
@Composable
fun WeekStrip(
    selectedDate: LocalDate,
    entries: List<JournalEntry>,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    birthdayMonthDay: Pair<Int, Int>? = null,
    flaggedDates: Set<LocalDate> = emptySet(),
    engagement: WeeklyEngagement? = null,
) {
    val c = MaterialTheme.modernColors
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val moodByDay: Map<LocalDate, Mood> = remember(entries) {
        entries.groupBy { it.date }.mapValues { (_, dayEntries) ->
            dayEntries.groupBy { it.mood }.maxByOrNull { it.value.size }!!.key
        }
    }
    val baseWeekStart = remember(today) { today.minus(DatePeriod(days = today.dayOfWeek.ordinal)) }

    var expanded by rememberSaveable { mutableStateOf(false) }
    var viewMonthEpochDay by rememberSaveable { mutableStateOf(firstOfMonth(selectedDate).toEpochDays()) }
    // The month view follows the selected date; month arrows move the view without changing selection.
    LaunchedEffect(selectedDate) {
        viewMonthEpochDay = firstOfMonth(selectedDate).toEpochDays()
    }
    val viewMonth = LocalDate.fromEpochDays(viewMonthEpochDay)

    Column(modifier.fillMaxWidth()) {
        // Expand / collapse control, top-right, using the fullscreen-style icon (out = expand to
        // month, in = collapse back to the week). The week's headline sits on the same line, so the
        // control reads as "there is more of this", not as a stray button.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (expanded) "This month" else "This week",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
            )
            Surface(
                onClick = { expanded = !expanded },
                shape = CircleShape,
                color = c.primary.copy(alpha = 0.10f),
            ) {
                Icon(
                    imageVector = if (expanded) PhosphorIcons.Regular.ArrowsInSimple else PhosphorIcons.Regular.ArrowsOutSimple,
                    contentDescription = if (expanded) "Collapse to week" else "Expand to month",
                    tint = c.primary,
                    modifier = Modifier.padding(7.dp).size(18.dp),
                )
            }
        }
        if (!expanded) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val weeksPerPage = if (maxWidth >= 600.dp) 2 else 1
                val pageCount = 105
                val center = pageCount / 2
                val pagerState = rememberPagerState(initialPage = center) { pageCount }
                HorizontalPager(state = pagerState) { page ->
                    val weekStart = baseWeekStart.plus(DatePeriod(days = (page - center) * 7 * weeksPerPage))
                    val days = (0 until 7 * weeksPerPage).map { weekStart.plus(DatePeriod(days = it)) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        days.forEach { day ->
                            DayCell(
                                day = day,
                                showLetter = true,
                                isSelected = day == selectedDate,
                                isToday = day == today,
                                isFuture = day > today,
                                mood = moodByDay[day],
                                isBirthday = birthdayMonthDay.matches(day),
                                isFlagged = day in flaggedDates,
                                onClick = { onSelect(day) },
                            )
                        }
                    }
                }
            }
        } else {
            MonthView(
                monthStart = viewMonth,
                selectedDate = selectedDate,
                today = today,
                moodByDay = moodByDay,
                flaggedDates = flaggedDates,
                birthdayMonthDay = birthdayMonthDay,
                onSelect = onSelect,
                onPrev = { viewMonthEpochDay = firstOfMonth(viewMonth.minus(DatePeriod(months = 1))).toEpochDays() },
                onNext = { viewMonthEpochDay = firstOfMonth(viewMonth.plus(DatePeriod(months = 1))).toEpochDays() },
            )
        }

        if (engagement != null) {
            Spacer(Modifier.height(8.dp))
            // Collapsed: one line you can read without stopping. Expanded: the same week as counts
            // you can actually compare.
            if (expanded) WeekOutputGrid(engagement) else WeekOutputLine(engagement)
        }
    }
}

/** The week's output as a single quiet sentence, sized for the collapsed banner. */
@Composable
private fun WeekOutputLine(e: WeeklyEngagement) {
    val c = MaterialTheme.modernColors
    val parts = buildList {
        if (e.habitCheckIns > 0) add("${e.habitCheckIns} check-in${plural(e.habitCheckIns)}")
        if (e.journalEntries > 0) add("${e.journalEntries} journal")
        if (e.focusSessionsCompleted > 0) add("${e.focusSessionsCompleted} focus")
        if (e.goalsCreated > 0) add("${e.goalsCreated} goal${plural(e.goalsCreated)}")
        if (e.aiCoachMessages > 0) add("${e.aiCoachMessages} coach")
    }
    Text(
        if (parts.isEmpty()) "Nothing logged yet this week. Anything you do lands here."
        else parts.joinToString(" · ") + " this week",
        style = MaterialTheme.typography.bodySmall,
        color = c.textSecondary,
    )
}

/** The expanded read of the same week: every kind of output the app tracks, as counts. */
@Composable
private fun WeekOutputGrid(e: WeeklyEngagement) {
    val c = MaterialTheme.modernColors
    val stats = listOf(
        Triple("Check-ins", e.habitCheckIns, WEEK_HABITS),
        Triple("Journal", e.journalEntries, WEEK_JOURNAL),
        Triple("Focus", e.focusSessionsCompleted, WEEK_FOCUS),
        Triple("Goals", e.goalsCreated, WEEK_GOALS),
        Triple("Coach", e.aiCoachMessages, WEEK_COACH),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value, color) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.08f))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            value.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = color,
                        )
                        Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary)
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

private fun plural(n: Int) = if (n == 1) "" else "s"

private val WEEK_HABITS = Color(0xFF4CAF50)
private val WEEK_GOALS = Color(0xFF6366F1)
private val WEEK_JOURNAL = Color(0xFFFF9800)
private val WEEK_FOCUS = Color(0xFF6C63FF)
private val WEEK_COACH = Color(0xFF26A69A)

@Composable
private fun MonthView(
    monthStart: LocalDate,
    selectedDate: LocalDate,
    today: LocalDate,
    moodByDay: Map<LocalDate, Mood>,
    flaggedDates: Set<LocalDate>,
    birthdayMonthDay: Pair<Int, Int>?,
    onSelect: (LocalDate) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val daysInMonth = (monthStart.plus(DatePeriod(months = 1)).toEpochDays() - monthStart.toEpochDays()).toInt()
    val lead = monthStart.dayOfWeek.ordinal // Mon=0
    val rows = (lead + daysInMonth + 6) / 7
    val monthName = monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(PhosphorIcons.Regular.CaretLeft, "Previous month", tint = c.textSecondary, modifier = Modifier.bouncyClickable(onClick = onPrev).size(20.dp))
            Text("$monthName ${monthStart.year}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = c.textPrimary)
            Icon(PhosphorIcons.Regular.CaretRight, "Next month", tint = c.textSecondary, modifier = Modifier.bouncyClickable(onClick = onNext).size(20.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = c.textTertiary)
            }
        }
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (col in 0 until 7) {
                    val dayNum = r * 7 + col - lead + 1
                    if (dayNum in 1..daysInMonth) {
                        val day = monthStart.plus(DatePeriod(days = dayNum - 1))
                        DayCell(
                            day = day,
                            showLetter = false,
                            isSelected = day == selectedDate,
                            isToday = day == today,
                            isFuture = day > today,
                            mood = moodByDay[day],
                            isBirthday = birthdayMonthDay.matches(day),
                            isFlagged = day in flaggedDates,
                            onClick = { onSelect(day) },
                        )
                    } else {
                        Box(Modifier.size(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    showLetter: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    mood: Mood?,
    isBirthday: Boolean,
    isFlagged: Boolean,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val emoji = when {
        isBirthday -> "🎂"
        mood != null -> mood.emoji
        else -> null
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(horizontal = 2.dp).bouncyClickable(onClick = onClick),
    ) {
        if (showLetter) {
            Text(
                day.dayOfWeek.name.take(1),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) c.primary else c.textTertiary,
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(38.dp)) {
            Surface(
                shape = CircleShape,
                color = when {
                    isSelected -> c.primary
                    isToday -> c.primary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                modifier = Modifier.size(36.dp),
            ) {}
            if (emoji != null) {
                Text(emoji, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = when {
                        isSelected -> Color.White
                        isFuture -> c.textTertiary.copy(alpha = 0.5f)
                        isToday -> c.primary
                        else -> c.textPrimary
                    },
                )
            }
        }
        Box(Modifier.size(5.dp).clip(CircleShape).background(if (isFlagged) c.secondary else Color.Transparent))
    }
}

private fun firstOfMonth(d: LocalDate): LocalDate = LocalDate(d.year, d.monthNumber, 1)

private fun Pair<Int, Int>?.matches(day: LocalDate): Boolean =
    this?.let { day.monthNumber == it.first && day.dayOfMonth == it.second } ?: false
