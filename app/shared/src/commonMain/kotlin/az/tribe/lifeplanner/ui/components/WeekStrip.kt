package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
        // month, in = collapse back to the week).
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
    }
}

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
