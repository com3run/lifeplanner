package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 * A compact, persistent "day lens": the Mon–Sun week containing [selectedDate], each day tappable.
 * The selected day is filled; today gets a ring. Shared across the hub tabs so the chosen day sticks
 * as the user moves between Journal / Habits.
 */
@Composable
fun WeekStrip(
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    // Monday-based week start (kotlinx DayOfWeek.ordinal: Mon=0 .. Sun=6).
    val weekStart = selectedDate.minus(DatePeriod(days = selectedDate.dayOfWeek.ordinal))
    val days = (0..6).map { weekStart.plus(DatePeriod(days = it)) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            DayCell(
                day = day,
                isSelected = day == selectedDate,
                isToday = day == today,
                enabled = day <= today, // no future days in this lens
                onClick = { if (day <= today) onSelect(day) },
            )
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val letter = day.dayOfWeek.name.take(1) // M T W T F S S
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .let { if (enabled) it.bouncyClickable(onClick = onClick) else it },
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
                    else -> androidx.compose.ui.graphics.Color.Transparent
                },
                modifier = Modifier.size(36.dp),
            ) {}
            Text(
                day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                ),
                color = when {
                    isSelected -> androidx.compose.ui.graphics.Color.White
                    !enabled -> c.textTertiary.copy(alpha = 0.4f)
                    isToday -> c.primary
                    else -> c.textPrimary
                },
            )
        }
    }
}
