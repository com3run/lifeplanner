package az.tribe.lifeplanner.ui.retrospective

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.ArrowUp
import com.adamglin.phosphoricons.regular.Minus
import com.adamglin.phosphoricons.regular.Trophy
import com.adamglin.phosphoricons.regular.Tray
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Badge
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.badgeIcon
import kotlinx.datetime.LocalDate

@Composable
internal fun BadgesSection(badges: List<Badge>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            badges.forEachIndexed { index, badge ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(badge.type.color).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon(badge.type),
                            contentDescription = badge.type.displayName,
                            modifier = Modifier.size(20.dp),
                            tint = Color(badge.type.color)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            badge.type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Earned on this day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        PhosphorIcons.Regular.Trophy,
                        contentDescription = null,
                        tint = Color(badge.type.color),
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (index < badges.size - 1) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Side-by-side compare card between a past day and today.
 * Always shown when viewing a past date that has today's data to compare with.
 */
@Composable
internal fun CompareSection(thenSnapshot: DaySnapshot, nowSnapshot: DaySnapshot, thenDate: LocalDate) {
    val month = thenDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val thenLabel = "$month ${thenDate.day}"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Then vs Now",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$thenLabel compared with today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    thenLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp)
                )
                Spacer(Modifier.width(28.dp))
                Text(
                    "Today",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            )
            Spacer(Modifier.height(8.dp))

            val habitsRateThen = if (thenSnapshot.habitSummary.totalHabits > 0)
                thenSnapshot.habitSummary.completedHabits * 100 / thenSnapshot.habitSummary.totalHabits else 0
            val habitsRateNow = if (nowSnapshot.habitSummary.totalHabits > 0)
                nowSnapshot.habitSummary.completedHabits * 100 / nowSnapshot.habitSummary.totalHabits else 0

            DeltaRow(
                label = "Habits",
                thenValue = "${thenSnapshot.habitSummary.completedHabits}/${thenSnapshot.habitSummary.totalHabits}",
                nowValue = "${nowSnapshot.habitSummary.completedHabits}/${nowSnapshot.habitSummary.totalHabits}",
                delta = habitsRateNow - habitsRateThen,
                deltaLabel = if (habitsRateNow != habitsRateThen) "${habitsRateNow - habitsRateThen}%" else null
            )
            DeltaRow(
                label = "Focus",
                thenValue = "${thenSnapshot.totalFocusMinutes}m",
                nowValue = "${nowSnapshot.totalFocusMinutes}m",
                delta = nowSnapshot.totalFocusMinutes - thenSnapshot.totalFocusMinutes,
                deltaLabel = buildDeltaLabel(nowSnapshot.totalFocusMinutes - thenSnapshot.totalFocusMinutes, "m")
            )
            DeltaRow(
                label = "Journal",
                thenValue = "${thenSnapshot.journalEntries.size}",
                nowValue = "${nowSnapshot.journalEntries.size}",
                delta = nowSnapshot.journalEntries.size - thenSnapshot.journalEntries.size,
                deltaLabel = buildDeltaLabel(nowSnapshot.journalEntries.size - thenSnapshot.journalEntries.size)
            )
            DeltaRow(
                label = "XP earned",
                thenValue = "+${thenSnapshot.xpEarnedOnDay}",
                nowValue = "+${nowSnapshot.xpEarnedOnDay}",
                delta = nowSnapshot.xpEarnedOnDay - thenSnapshot.xpEarnedOnDay,
                deltaLabel = buildDeltaLabel(nowSnapshot.xpEarnedOnDay - thenSnapshot.xpEarnedOnDay)
            )
        }
    }
}

private fun buildDeltaLabel(delta: Int, suffix: String = ""): String? {
    return when {
        delta > 0 -> "+$delta$suffix"
        delta < 0 -> "$delta$suffix"
        else -> null
    }
}

@Composable
private fun DeltaRow(
    label: String,
    thenValue: String,
    nowValue: String,
    delta: Int,
    deltaLabel: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            thenValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(52.dp)
        )
        // Delta indicator
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                delta > 0 -> DeltaBadge(icon = PhosphorIcons.Regular.ArrowUp, color = Color(0xFF4CAF50), label = deltaLabel)
                delta < 0 -> DeltaBadge(icon = PhosphorIcons.Regular.ArrowDown, color = MaterialTheme.colorScheme.error, label = deltaLabel)
                else -> Icon(PhosphorIcons.Regular.Minus, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(12.dp))
            }
        }
        Text(
            nowValue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(52.dp)
        )
    }
}

@Composable
private fun DeltaBadge(icon: ImageVector, color: Color, label: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
    }
}

@Composable
internal fun EmptyDayState() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                PhosphorIcons.Regular.Tray,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No activity recorded",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Nothing was tracked on this date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
