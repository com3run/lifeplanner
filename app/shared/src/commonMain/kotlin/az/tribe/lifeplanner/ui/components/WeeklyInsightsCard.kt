package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.model.HealthMetric
import az.tribe.lifeplanner.domain.model.HabitDayStatus
import az.tribe.lifeplanner.ui.theme.CategoryColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Note
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.X
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

// ── Colors ───────────────────────────────────────────────────────────────────

private val COLOR_HABIT   = Color(0xFF6366F1)
private val COLOR_FOCUS   = Color(0xFF06B6D4)
private val COLOR_STEPS   = Color(0xFF28C76F)
private val COLOR_JOURNAL = Color(0xFFFF9800)
private val COLOR_GOAL_LINKED = Color(0xFF6366F1)
private val COLOR_STANDALONE  = Color(0xFF4CAF50)

private const val STEPS_TARGET = 10_000.0

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY    -> "Mon"
    DayOfWeek.TUESDAY   -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY  -> "Thu"
    DayOfWeek.FRIDAY    -> "Fri"
    DayOfWeek.SATURDAY  -> "Sat"
    DayOfWeek.SUNDAY    -> "Sun"
}

private fun Mood?.toEmoji(): String = when (this) {
    Mood.VERY_HAPPY -> "😁"
    Mood.HAPPY      -> "🙂"
    Mood.NEUTRAL    -> "😐"
    Mood.SAD        -> "😕"
    Mood.VERY_SAD   -> "😞"
    null            -> "·"
}

private fun GoalCategory.dotColor(): Color = when (this) {
    GoalCategory.CAREER    -> CategoryColors.CAREER
    GoalCategory.MONEY     -> CategoryColors.MONEY
    GoalCategory.BODY      -> CategoryColors.BODY
    GoalCategory.PEOPLE    -> CategoryColors.PEOPLE
    GoalCategory.WELLBEING -> CategoryColors.WELLBEING
    GoalCategory.PURPOSE   -> CategoryColors.PURPOSE
    GoalCategory.FAMILY    -> CategoryColors.FAMILY
}


// ── Card ─────────────────────────────────────────────────────────────────────

@Composable
fun WeeklyInsightsCard(
    snapshots: List<DaySnapshot>,
    onDayClick: (LocalDate) -> Unit,
    stepsHistory: List<HealthMetric> = emptyList(),
    personalizedInsight: String? = null,
    modifier: Modifier = Modifier
) {
    if (snapshots.isEmpty()) return

    val stepsByDate = stepsHistory.associate { it.date to it.value }

    // Count days that have real data
    val activeDays = snapshots.count { snap ->
        snap.hasAnyActivity || (stepsByDate[snap.date] ?: 0.0) > 0.0
    }
    val todaySnap = snapshots.lastOrNull() ?: return

    // Not enough data for a week chart — show today only
    if (activeDays < 2) {
        TodayOnlyCard(
            snap = todaySnap,
            stepsToday = stepsByDate[todaySnap.date]?.toLong(),
            onDayClick = onDayClick,
            modifier = modifier
        )
        return
    }

    // ── Full week chart ───────────────────────────────────────────────────────

    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animFraction.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }
    val fraction = animFraction.value

    var expandedDate by rememberSaveable { mutableStateOf<String?>(null) }

    val weekGoalLinked = snapshots.sumOf { it.habitSummary.habits.count { h -> h.linkedGoalId != null && h.wasCompleted } }
    val weekStandalone = snapshots.sumOf { it.habitSummary.habits.count { h -> h.linkedGoalId == null && h.wasCompleted } }
    val weekJournal    = snapshots.sumOf { it.journalEntries.size }
    val anyFocus       = snapshots.any { it.totalFocusMinutes > 0 }
    val maxFocus       = snapshots.maxOf { it.totalFocusMinutes }.coerceAtLeast(1)
    val weekTotalSteps = stepsByDate.values.sumOf { it }.toLong()
    val anySteps = stepsHistory.any { it.value > 0 }

    // Per-day data
    val habitRates = snapshots.map { snap ->
        val total = snap.habitSummary.totalHabits
        if (total > 0) snap.habitSummary.completedHabits.toFloat() / total else 0f
    }
    val focusRates = snapshots.map { it.totalFocusMinutes.toFloat() / maxFocus }
    val stepsRates = snapshots.map { snap ->
        ((stepsByDate[snap.date] ?: 0.0) / STEPS_TARGET).toFloat().coerceIn(0f, 1f)
    }

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "This Week",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    buildString {
                        if (weekGoalLinked > 0) append("$weekGoalLinked goal · ")
                        append("$weekStandalone habit")
                        if (weekJournal > 0) append(" · $weekJournal journal")
                        if (weekTotalSteps > 0) append(" · ${weekTotalSteps / 1000}K steps")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }

            if (!personalizedInsight.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    personalizedInsight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    lineHeight = 17.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Bar chart (Compose-native) ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                snapshots.forEachIndexed { i, snap ->
                    val isSelected = expandedDate == snap.date.toString()
                    val isToday = i == snapshots.size - 1
                    val habitRate = habitRates[i]
                    val focusRate = focusRates[i]
                    val stepsRate = stepsRates[i]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) COLOR_HABIT.copy(alpha = 0.07f)
                                else Color.Transparent
                            )
                            .clickable { expandedDate = if (isSelected) null else snap.date.toString() }
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mood emoji
                        Text(
                            snap.dominantMood.toEmoji(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        // Bar area — fixed height, grows from bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Track background
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = if (isToday) 0.10f else 0.06f
                                        )
                                    )
                            )
                            // Habit fill bar (grows from bottom)
                            if (habitRate * fraction > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(habitRate * fraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (isToday) Color(0xFF818CF8) else COLOR_HABIT.copy(alpha = 0.82f),
                                                    Color(0xFF4338CA)
                                                )
                                            )
                                        )
                                )
                            }
                            // Focus stripe — thin, right edge
                            if (anyFocus && focusRate * fraction > 0.02f) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight(focusRate * fraction * 0.9f)
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(COLOR_FOCUS.copy(alpha = if (isToday) 1f else 0.78f))
                                )
                            }
                            // Steps dot — fixed position inside bar when steps present
                            if (anySteps && stepsRate * fraction > 0.08f) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = -(stepsRate * fraction * 110f).dp)
                                        .clip(CircleShape)
                                        .background(COLOR_STEPS)
                                )
                            }
                            // Journal dot — top of track
                            if (snap.journalEntries.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = 4.dp)
                                        .clip(CircleShape)
                                        .background(COLOR_JOURNAL)
                                )
                            }
                        }

                        // Selected accent line
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth(if (isSelected) 0.55f else 0f)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isSelected) COLOR_HABIT else Color.Transparent)
                        )

                        // Day label
                        Text(
                            snap.date.dayOfWeek.shortLabel(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) COLOR_HABIT
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


            // ── Expanded day detail ───────────────────────────────────────
            val selectedSnap = snapshots.find { it.date.toString() == expandedDate }
            AnimatedVisibility(
                visible = selectedSnap != null,
                enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                if (selectedSnap != null) {
                    DayDetailPanel(
                        snap = selectedSnap,
                        stepsForDay = stepsByDate[selectedSnap.date]?.toLong(),
                        onDayClick = onDayClick
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Legend ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeeklyLegendDot(COLOR_HABIT, "Habits")
                if (anyFocus) WeeklyLegendDot(COLOR_FOCUS, "Focus")
                if (anySteps) WeeklyLegendDot(COLOR_STEPS, "Steps")
                WeeklyLegendDot(COLOR_JOURNAL, "Journal")
            }
        }
    }
}

// ── Today-only card (shown when < 2 days have data) ──────────────────────────

@Composable
private fun TodayOnlyCard(
    snap: DaySnapshot,
    stepsToday: Long?,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = snap.date.dayOfWeek.shortLabel()
    val habitsDone = snap.habitSummary.completedHabits
    val habitsTotal = snap.habitSummary.totalHabits

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                if (snap.dominantMood != null) {
                    Text(snap.dominantMood.toEmoji(), fontSize = 22.sp)
                }
            }

            // Stat tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Habits
                TodayStatTile(
                    value = if (habitsTotal > 0) "$habitsDone/$habitsTotal" else "—",
                    label = "habits",
                    color = COLOR_HABIT,
                    modifier = Modifier.weight(1f)
                )
                // Focus
                TodayStatTile(
                    value = if (snap.totalFocusMinutes > 0) "${snap.totalFocusMinutes}m" else "—",
                    label = "focus",
                    color = COLOR_FOCUS,
                    modifier = Modifier.weight(1f)
                )
                // Steps
                if (stepsToday != null) {
                    TodayStatTile(
                        value = if (stepsToday >= 1000) "${stepsToday / 1000}K" else "$stepsToday",
                        label = "steps",
                        color = COLOR_STEPS,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Journal
                if (snap.journalEntries.isNotEmpty()) {
                    TodayStatTile(
                        value = "${snap.journalEntries.size}",
                        label = "journal",
                        color = COLOR_JOURNAL,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Motivational line based on today's completion
            val habitRate = if (habitsTotal > 0) habitsDone.toFloat() / habitsTotal else 0f
            val motivation = when {
                habitRate >= 1f -> "Perfect day — all habits done! 🏆"
                habitRate >= 0.6f -> "Great progress today. Keep going!"
                habitsDone > 0 -> "Good start. More to check off."
                snap.totalFocusMinutes > 0 -> "Focus session done. Track your habits too."
                else -> "Start your first habit to see your week build."
            }
            Text(
                motivation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (snap.hasAnyActivity) {
                Text(
                    "See full day →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onDayClick(snap.date) }
                )
            }
        }
    }
}

@Composable
private fun TodayStatTile(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.7f)
        )
    }
}

// ── Day detail panel ──────────────────────────────────────────────────────────

@Composable
private fun DayDetailPanel(snap: DaySnapshot, stepsForDay: Long?, onDayClick: (LocalDate) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (snap.habitSummary.habits.isNotEmpty()) {
            Text(
                "Habits",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            snap.habitSummary.habits.forEach { HabitDetailRow(it) }
        }

        val hasStats = snap.journalEntries.isNotEmpty() || snap.totalFocusMinutes > 0 ||
                snap.goalChanges.isNotEmpty() || (stepsForDay != null && stepsForDay > 0)
        if (hasStats && snap.habitSummary.habits.isNotEmpty()) Spacer(Modifier.height(10.dp))

        if (hasStats) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (snap.journalEntries.isNotEmpty()) {
                    StatChip({ Icon(PhosphorIcons.Regular.Note, null, Modifier.size(12.dp), COLOR_JOURNAL) },
                        "${snap.journalEntries.size} journal", COLOR_JOURNAL)
                }
                if (snap.totalFocusMinutes > 0) {
                    StatChip({ Icon(PhosphorIcons.Regular.Timer, null, Modifier.size(12.dp), COLOR_FOCUS) },
                        "${snap.totalFocusMinutes}m focus", COLOR_FOCUS)
                }
                if (stepsForDay != null && stepsForDay > 0) {
                    val stepsLabel = if (stepsForDay >= 1000) "${stepsForDay / 1000}K steps" else "$stepsForDay steps"
                    StatChip({ Text("👟", fontSize = 10.sp) }, stepsLabel, COLOR_STEPS)
                }
                if (snap.goalChanges.isNotEmpty()) {
                    StatChip({ Icon(PhosphorIcons.Regular.TrendUp, null, Modifier.size(12.dp), Color(0xFF00C853)) },
                        "${snap.goalChanges.size} goal update", Color(0xFF00C853))
                }
            }
        }

        if (!snap.hasAnyActivity) {
            Text(
                "No activity recorded",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "See full day →",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onDayClick(snap.date) }
        )
    }
}

@Composable
private fun HabitDetailRow(habit: HabitDayStatus) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(habit.category.dotColor()))
        Icon(
            imageVector = if (habit.wasCompleted) PhosphorIcons.Regular.Check else PhosphorIcons.Regular.X,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = if (habit.wasCompleted) Color(0xFF4CAF50)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = if (habit.wasCompleted) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (habit.linkedGoalId != null) {
            Icon(PhosphorIcons.Regular.TrendUp, null, Modifier.size(11.dp), COLOR_GOAL_LINKED.copy(0.7f))
        }
    }
}

@Composable
private fun StatChip(icon: @Composable () -> Unit, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        icon()
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WeeklyLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
