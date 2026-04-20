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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.domain.model.HabitDayStatus
import az.tribe.lifeplanner.ui.theme.CategoryColors
import az.tribe.lifeplanner.domain.enum.GoalCategory
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Note
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.X
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

// ── Colors ───────────────────────────────────────────────────────────────────

private val COLOR_GOAL_LINKED = Color(0xFF6366F1)
private val COLOR_STANDALONE  = Color(0xFF4CAF50)
private val COLOR_JOURNAL     = Color(0xFFFF9800)
private val COLOR_FOCUS       = Color(0xFF6C63FF)

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
    null            -> ""
}

private fun GoalCategory.dotColor(): Color = when (this) {
    GoalCategory.CAREER    -> CategoryColors.CAREER
    GoalCategory.MONEY     -> CategoryColors.MONEY
    GoalCategory.BODY      -> CategoryColors.BODY
    GoalCategory.PEOPLE    -> CategoryColors.PEOPLE
    GoalCategory.WELLBEING -> CategoryColors.WELLBEING
    GoalCategory.PURPOSE   -> CategoryColors.PURPOSE
}

// ── Card ─────────────────────────────────────────────────────────────────────

@Composable
fun WeeklyInsightsCard(
    snapshots: List<DaySnapshot>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (snapshots.isEmpty()) return

    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animFraction.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val fraction = animFraction.value

    var expandedDate by rememberSaveable { mutableStateOf<String?>(null) }

    val weekGoalLinked  = snapshots.sumOf { it.habitSummary.habits.count { h -> h.linkedGoalId != null && h.wasCompleted } }
    val weekStandalone  = snapshots.sumOf { it.habitSummary.habits.count { h -> h.linkedGoalId == null && h.wasCompleted } }
    val weekJournal     = snapshots.sumOf { it.journalEntries.size }
    val maxCompletedInWeek = snapshots.maxOfOrNull { it.habitSummary.completedHabits } ?: 1

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ───────────────────────────────────────────────
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
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Day bars ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                snapshots.forEach { snap ->
                    val isExpanded = expandedDate == snap.date.toString()
                    val total      = snap.habitSummary.totalHabits
                    val completed  = snap.habitSummary.completedHabits
                    val habitFraction = if (maxCompletedInWeek > 0) completed.toFloat() / maxCompletedInWeek else 0f
                    val hasJournal = snap.journalEntries.isNotEmpty()
                    val hasFocus   = snap.totalFocusMinutes > 0
                    val mood       = snap.dominantMood
                    val goalLinked = snap.habitSummary.habits.count { it.linkedGoalId != null && it.wasCompleted }
                    val standalone = completed - goalLinked

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isExpanded)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable {
                                expandedDate = if (isExpanded) null else snap.date.toString()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = mood.toEmoji(),
                            fontSize = 12.sp,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(Modifier.height(4.dp))

                        val barMaxHeight = 52.dp

                        Box(modifier = Modifier.fillMaxWidth().height(barMaxHeight)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f))
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (hasFocus) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp * fraction)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(COLOR_FOCUS.copy(alpha = 0.7f))
                                    )
                                }
                                if (hasJournal) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp * fraction)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(COLOR_JOURNAL.copy(alpha = 0.8f))
                                    )
                                }
                                if (total > 0 && completed > 0) {
                                    val barH = barMaxHeight * habitFraction.coerceIn(0f, 1f) * fraction
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barH)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    ) {
                                        when {
                                            completed == 0 -> Box(
                                                modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            )
                                            goalLinked > 0 && standalone > 0 -> {
                                                Box(modifier = Modifier.fillMaxWidth().weight(goalLinked.toFloat())
                                                    .background(COLOR_GOAL_LINKED.copy(alpha = if (habitFraction >= 1f) 1f else 0.75f)))
                                                Box(modifier = Modifier.fillMaxWidth().weight(standalone.toFloat())
                                                    .background(COLOR_STANDALONE.copy(alpha = if (habitFraction >= 1f) 1f else 0.55f)))
                                            }
                                            goalLinked > 0 -> Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                                .background(COLOR_GOAL_LINKED.copy(alpha = if (habitFraction >= 1f) 1f else 0.75f)))
                                            else -> Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                                .background(COLOR_STANDALONE.copy(alpha = if (habitFraction >= 1f) 1f else 0.55f)))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            snap.date.dayOfWeek.shortLabel(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isExpanded)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
                    DayDetailPanel(snap = selectedSnap, onDayClick = onDayClick)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Legend ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeeklyLegendDot(COLOR_STANDALONE,  "Habits")
                WeeklyLegendDot(COLOR_GOAL_LINKED, "Goal-linked")
                WeeklyLegendDot(COLOR_JOURNAL,     "Journal")
                WeeklyLegendDot(COLOR_FOCUS,       "Focus")
            }
        }
    }
}

// ── Expanded day detail panel ─────────────────────────────────────────────────

@Composable
private fun DayDetailPanel(snap: DaySnapshot, onDayClick: (LocalDate) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ── Habit list ───────────────────────────────────────────────
        if (snap.habitSummary.habits.isNotEmpty()) {
            Text(
                "Habits",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            snap.habitSummary.habits.forEach { habit ->
                HabitDetailRow(habit)
            }
        }

        // ── Stats row (journal / focus / goal changes) ───────────────
        val hasStats = snap.journalEntries.isNotEmpty() ||
                snap.totalFocusMinutes > 0 ||
                snap.goalChanges.isNotEmpty()

        if (hasStats && snap.habitSummary.habits.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
        }

        if (hasStats) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (snap.journalEntries.isNotEmpty()) {
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.Note, null, modifier = Modifier.size(12.dp), tint = COLOR_JOURNAL) },
                        label = "${snap.journalEntries.size} journal",
                        color = COLOR_JOURNAL
                    )
                }
                if (snap.totalFocusMinutes > 0) {
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.Timer, null, modifier = Modifier.size(12.dp), tint = COLOR_FOCUS) },
                        label = "${snap.totalFocusMinutes}m focus",
                        color = COLOR_FOCUS
                    )
                }
                if (snap.goalChanges.isNotEmpty()) {
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.TrendUp, null, modifier = Modifier.size(12.dp), tint = Color(0xFF00C853)) },
                        label = "${snap.goalChanges.size} goal update",
                        color = Color(0xFF00C853)
                    )
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

        // ── Open full day link ────────────────────────────────────────
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category colour dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(habit.category.dotColor())
        )
        // Check / cross icon
        Icon(
            imageVector = if (habit.wasCompleted) PhosphorIcons.Regular.Check else PhosphorIcons.Regular.X,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = if (habit.wasCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
        // Habit title
        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = if (habit.wasCompleted)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        // Goal-linked badge
        if (habit.linkedGoalId != null) {
            Icon(
                imageVector = PhosphorIcons.Regular.TrendUp,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = COLOR_GOAL_LINKED.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatChip(
    icon: @Composable () -> Unit,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        icon()
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Legend ────────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
