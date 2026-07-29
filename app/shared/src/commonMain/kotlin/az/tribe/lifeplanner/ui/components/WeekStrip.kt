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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * A scrollable "day lens", mood-calendar style: swipe left/right through weeks, each day showing its
 * journal mood emoji (or a birthday cake), with today ringed and the selected day filled. Wide
 * screens show two weeks per page. Shared across the hub tabs so the chosen day sticks.
 *
 * [birthdayMonthDay] is (month 1-12, day); when a day matches, it shows a 🎂. Null until the app
 * captures the user's birthday.
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
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    // Dominant mood per day, for the smile on each cell.
    val moodByDay: Map<LocalDate, Mood> = remember(entries) {
        entries.groupBy { it.date }.mapValues { (_, dayEntries) ->
            dayEntries.groupBy { it.mood }.maxByOrNull { it.value.size }!!.key
        }
    }
    val baseWeekStart = remember(today) { today.minus(DatePeriod(days = today.dayOfWeek.ordinal)) }

    BoxWithConstraints(modifier.fillMaxWidth()) {
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
                        isSelected = day == selectedDate,
                        isToday = day == today,
                        isFuture = day > today,
                        mood = moodByDay[day],
                        isBirthday = birthdayMonthDay?.let { day.monthNumber == it.first && day.dayOfMonth == it.second } ?: false,
                        isFlagged = day in flaggedDates,
                        onClick = { onSelect(day) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    mood: Mood?,
    isBirthday: Boolean,
    isFlagged: Boolean,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val letter = day.dayOfWeek.name.take(1)
    // Emoji takes priority as the day's "face"; otherwise show the date number.
    val emoji = when {
        isBirthday -> "🎂"
        mood != null -> mood.emoji
        else -> null
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .bouncyClickable(onClick = onClick),
    ) {
        Text(
            letter,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) c.primary else c.textTertiary,
        )
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
        // A small flag dot marks days with a goal/milestone due.
        Box(
            modifier = Modifier.size(5.dp).clip(CircleShape)
                .background(if (isFlagged) c.secondary else Color.Transparent),
        )
    }
}
