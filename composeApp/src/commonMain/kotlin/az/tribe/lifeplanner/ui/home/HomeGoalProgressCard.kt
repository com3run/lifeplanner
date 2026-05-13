package az.tribe.lifeplanner.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.backgroundColor
import az.tribe.lifeplanner.ui.theme.gradientColors
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable
fun HomeGoalProgressCard(
    goals: List<Goal>,
    weeklySnapshots: List<DaySnapshot>,
    today: LocalDate,
    onGoalsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeGoals = goals
        .filter { it.status != GoalStatus.COMPLETED && !it.isArchived }
        .sortedBy { it.dueDate }
        .take(3)

    if (activeGoals.isEmpty()) return

    val weekHabitsTotal = weeklySnapshots.sumOf { it.habitSummary.totalHabits }
    val weekHabitsDone = weeklySnapshots.sumOf { it.habitSummary.completedHabits }
    val weekFocusMin = weeklySnapshots.sumOf { it.totalFocusMinutes }

    val avgActual = if (activeGoals.isEmpty()) 0f else
        activeGoals.map { (it.progress ?: 0L).toFloat() }.average().toFloat()

    val overallStatus = run {
        val diffs = activeGoals.map { g ->
            (g.progress ?: 0L).toFloat() / 100f - idealProgress(g, today)
        }
        val avg = diffs.average()
        when {
            avg > 0.05 -> ProgressStatus.AHEAD
            avg > -0.15 -> ProgressStatus.ON_TRACK
            else -> ProgressStatus.BEHIND
        }
    }

    val aheadCount = activeGoals.count { g ->
        (g.progress ?: 0L).toFloat() / 100f >= idealProgress(g, today) + 0.08f
    }
    val behindCount = activeGoals.count { g ->
        (g.progress ?: 0L).toFloat() / 100f < idealProgress(g, today) - 0.15f
    }
    val onTrackCount = activeGoals.size - aheadCount - behindCount

    val motivationalText = when (overallStatus) {
        ProgressStatus.AHEAD -> "You're ahead of schedule. Keep the momentum!"
        ProgressStatus.ON_TRACK -> "Consistent progress. You're on the right path."
        ProgressStatus.BEHIND -> "A few goals need attention. Small daily steps add up."
    }

    // Animated counter for the avg %
    var displayedAvg by remember { mutableIntStateOf(0) }
    LaunchedEffect(avgActual) {
        val target = avgActual.toInt()
        val step = if (target > displayedAvg) 1 else -1
        while (displayedAvg != target) {
            displayedAvg += step
            delay(14)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Section header ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Goal Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${activeGoals.size} active goal${if (activeGoals.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            StatusSummaryChips(aheadCount = aheadCount, onTrackCount = onTrackCount, behindCount = behindCount)
        }

        // ── Main card ─────────────────────────────────────────────────────
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
            Column(modifier = Modifier.padding(20.dp)) {

                // ── Hero stat row ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "$displayedAvg",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 40.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "avg completion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier.width(1.dp).height(44.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            motivationalText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        if (weekHabitsTotal > 0 || weekFocusMin > 0) {
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (weekHabitsTotal > 0) {
                                    MiniStatChip(
                                        text = "$weekHabitsDone/$weekHabitsTotal habits",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (weekFocusMin > 0) {
                                    MiniStatChip(
                                        text = "${weekFocusMin}m focus",
                                        color = Color(0xFF00CFE8)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )
                Spacer(Modifier.height(20.dp))

                // ── Goal rows ─────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    activeGoals.forEach { goal ->
                        GoalProgressRow(goal = goal, today = today)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalProgressRow(goal: Goal, today: LocalDate) {
    val color = goal.category.backgroundColor()
    val gradColors = goal.category.gradientColors()
    val actual = (goal.progress ?: 0L).toFloat() / 100f
    val ideal = idealProgress(goal, today)
    val delta = ((actual - ideal) * 100).toInt()  // percentage points difference
    val status = when {
        delta > 8 -> ProgressStatus.AHEAD
        delta > -15 -> ProgressStatus.ON_TRACK
        else -> ProgressStatus.BEHIND
    }
    val daysLeft = (goal.dueDate.toEpochDays() - today.toEpochDays()).toInt()

    val animActual = remember(goal.id) { Animatable(0f) }
    val animIdeal = remember(goal.id) { Animatable(0f) }
    LaunchedEffect(goal.id, actual) {
        animActual.animateTo(actual, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(goal.id, ideal) {
        animIdeal.animateTo(ideal, tween(800, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: category icon badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(gradColors.map { it.copy(alpha = 0.15f) })),
            contentAlignment = Alignment.Center
        ) {
            Text(goal.category.icon(), fontSize = 20.sp)
        }

        // Center: title + bars
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                // Days left chip
                Text(
                    when {
                        daysLeft < 0 -> "Overdue"
                        daysLeft == 0 -> "Due today"
                        daysLeft <= 7 -> "${daysLeft}d left"
                        else -> "${daysLeft}d"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        daysLeft < 0 -> Color(0xFFEA5455)
                        daysLeft <= 3 -> Color(0xFFFF9F43)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }

            // Dual-layer progress track — 10dp tall
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color.copy(alpha = 0.08f))
            ) {
                // Ideal position — muted fill up to target
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animIdeal.value.coerceIn(0f, 1f))
                        .height(10.dp)
                        .background(color.copy(alpha = 0.22f), RoundedCornerShape(5.dp))
                )
                // Actual progress — gradient fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animActual.value.coerceIn(0f, 1f))
                        .height(10.dp)
                        .background(
                            Brush.horizontalGradient(gradColors),
                            RoundedCornerShape(5.dp)
                        )
                )
            }

            // Sub-labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "target ${(ideal * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                DeltaPill(delta = delta, status = status)
            }
        }

        // Right: big % number
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(52.dp)) {
            Text(
                "${(goal.progress ?: 0L).toInt()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                lineHeight = 28.sp
            )
            Text(
                "%",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DeltaPill(delta: Int, status: ProgressStatus) {
    if (delta == 0) return
    val (bg, text) = when (status) {
        ProgressStatus.AHEAD -> Color(0xFF28C76F).copy(alpha = 0.14f) to Color(0xFF28C76F)
        ProgressStatus.ON_TRACK -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ProgressStatus.BEHIND -> Color(0xFFFF9F43).copy(alpha = 0.14f) to Color(0xFFFF9F43)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            if (delta > 0) "+${delta}pp" else "${delta}pp",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = text
        )
    }
}

@Composable
private fun StatusSummaryChips(aheadCount: Int, onTrackCount: Int, behindCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (aheadCount > 0) {
            StatusDot(color = Color(0xFF28C76F), count = aheadCount, label = "ahead")
        }
        if (onTrackCount > 0) {
            StatusDot(color = MaterialTheme.colorScheme.primary, count = onTrackCount, label = "on track")
        }
        if (behindCount > 0) {
            StatusDot(color = Color(0xFFFF9F43), count = behindCount, label = "behind")
        }
    }
}

@Composable
private fun StatusDot(color: Color, count: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(7.dp).clip(CircleShape).background(color)
        )
        Text(
            "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MiniStatChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

private enum class ProgressStatus { AHEAD, ON_TRACK, BEHIND }

private fun idealProgress(goal: Goal, today: LocalDate): Float {
    val start = goal.createdAt.date
    val end = goal.dueDate
    val totalDays = (end.toEpochDays() - start.toEpochDays()).toFloat()
    if (totalDays <= 0f) return 1f
    val elapsed = (today.toEpochDays() - start.toEpochDays()).toFloat()
    return (elapsed / totalDays).coerceIn(0f, 1f)
}

private fun GoalCategory.icon() = when (this) {
    GoalCategory.CAREER -> "💼"
    GoalCategory.MONEY -> "💰"
    GoalCategory.BODY -> "💪"
    GoalCategory.PEOPLE -> "👥"
    GoalCategory.WELLBEING -> "💛"
    GoalCategory.PURPOSE -> "🙏"
    GoalCategory.FAMILY -> "🏡"
}
