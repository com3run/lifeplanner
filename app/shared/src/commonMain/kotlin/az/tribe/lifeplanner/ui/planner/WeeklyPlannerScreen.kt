package az.tribe.lifeplanner.ui.planner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.habit.HabitWithStatus
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.illus_empty_calendar
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

// ─── Domain types ─────────────────────────────────────────────────────────────

private enum class EnergyBlock(
    val label: String,
    val timeRange: String,
    val subtitle: String,
    val color: Color
) {
    MORNING("Morning Peak", "5am, 12pm", "Deep work window", Color(0xFFF59E0B)),
    AFTERNOON("Afternoon Flow", "12pm, 5pm", "Collaboration zone", Color(0xFFEF4444)),
    EVENING("Evening Reset", "5pm, 11pm", "Reflection mode", Color(0xFF6366F1)),
    ANYTIME("Flexible", "Anytime", "On your schedule", Color(0xFF6B7280))
}

private data class PlannerInsight(val concept: String, val body: String, val source: String)

private val INSIGHTS = listOf(
    PlannerInsight("90-min focus cycles", "Align deep work to your natural alert window, brains cycle every ~90 min.", "Kleitman & Lavie"),
    PlannerInsight("Implementation intentions", "Naming when + where you'll act makes follow-through 2-3× more likely.", "Gollwitzer, 1999"),
    PlannerInsight("Weekly review ritual", "A 30-min weekly review reduces decision fatigue and sharpens priorities.", "Allen, GTD"),
    PlannerInsight("Time blocking", "Scheduling deep work in advance shields it from shallow-task drift.", "Cal Newport"),
    PlannerInsight("Energy over time", "Managing energy, not just hours, sustains high performance long-term.", "Loehr & Schwartz"),
    PlannerInsight("Habit stacking", "Anchoring a new habit to an existing cue cuts friction to start by ~50%.", "BJ Fogg"),
    PlannerInsight("Ultradian rhythms", "Post-lunch dips are biology, not laziness, schedule creative work earlier.", "Circadian research"),
)

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun habitEnergyBlock(reminderTime: String?): EnergyBlock {
    val hour = reminderTime?.split(":")?.firstOrNull()?.toIntOrNull() ?: return EnergyBlock.ANYTIME
    return when (hour) {
        in 5..11 -> EnergyBlock.MORNING
        in 12..16 -> EnergyBlock.AFTERNOON
        in 17..23 -> EnergyBlock.EVENING
        else -> EnergyBlock.ANYTIME
    }
}

private fun categoryAccent(category: GoalCategory): Color = when (category) {
    GoalCategory.CAREER -> Color(0xFFB39DDB)
    GoalCategory.MONEY -> Color(0xFF81C784)
    GoalCategory.BODY -> Color(0xFFEF9A9A)
    GoalCategory.PEOPLE -> Color(0xFF90CAF9)
    GoalCategory.WELLBEING -> Color(0xFFFFCC80)
    GoalCategory.PURPOSE -> Color(0xFFCE93D8)
    GoalCategory.FAMILY -> Color(0xFFF48FB1)
}

// ─── Entry point ──────────────────────────────────────────────────────────────

@Composable
fun WeeklyPlannerContent(
    habitsWithStatus: List<HabitWithStatus>,
    onCheckIn: (String) -> Unit,
    activeGoalCount: Int,
    modifier: Modifier = Modifier,
    viewModel: WeeklyPlannerViewModel = koinViewModel()
) {
    val weekStart by viewModel.weekStart.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val weekIntention by viewModel.weekIntention.collectAsState()
    val today = remember { viewModel.today() }
    val weekDays = remember(weekStart) { viewModel.weekDays() }
    val isCurrentWeek = remember(weekStart) { viewModel.isCurrentWeek() }

    val habitsByBlock = remember(habitsWithStatus) {
        habitsWithStatus.groupBy { habitEnergyBlock(it.habit.reminderTime) }
    }
    val dayIndex = remember(selectedDay) { selectedDay.dayOfWeek.ordinal }
    val insight = remember(today) { INSIGHTS[today.dayOfWeek.ordinal % INSIGHTS.size] }
    val filledBlocks = remember(habitsByBlock) {
        listOf(EnergyBlock.MORNING, EnergyBlock.AFTERNOON, EnergyBlock.EVENING, EnergyBlock.ANYTIME)
            .filter { (habitsByBlock[it]?.size ?: 0) > 0 }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        WeekHeader(weekStart, onPrevious = { viewModel.previousWeek() }, onNext = { viewModel.nextWeek() })
        Spacer(Modifier.height(10.dp))
        WeekDayStrip(weekDays, today, selectedDay, habitsWithStatus, isCurrentWeek) { viewModel.selectDay(it) }
        Spacer(Modifier.height(16.dp))
        WeekIntentionCard(weekIntention) { viewModel.updateIntention(it) }
        Spacer(Modifier.height(12.dp))
        ResearchInsightChip(insight)
        Spacer(Modifier.height(20.dp))

        val dayName = selectedDay.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val monthName = selectedDay.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val dayLabel = buildString {
            if (selectedDay == today) append("Today · ")
            append("$dayName, $monthName ${selectedDay.dayOfMonth}")
        }
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (filledBlocks.isEmpty()) {
            PlannerEmptyState()
        } else {
            filledBlocks.forEach { block ->
                EnergyBlockCard(
                    block = block,
                    habits = habitsByBlock[block] ?: emptyList(),
                    isToday = selectedDay == today,
                    dayIndexInWeek = dayIndex,
                    isCurrentWeek = isCurrentWeek,
                    onCheckIn = onCheckIn
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        WeeklyStatsRow(
            completedToday = habitsWithStatus.count { it.isCompletedToday },
            totalHabits = habitsWithStatus.size,
            activeGoals = activeGoalCount,
            topStreak = habitsWithStatus.maxOfOrNull { it.habit.currentStreak } ?: 0
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ─── Week header ──────────────────────────────────────────────────────────────

@Composable
private fun WeekHeader(weekStart: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    val weekEnd = remember(weekStart) { weekStart.plus(6, DateTimeUnit.DAY) }
    val startMonthName = weekStart.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val endMonthName = weekEnd.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val label = if (weekStart.month == weekEnd.month) {
        "$startMonthName ${weekStart.year}"
    } else {
        "$startMonthName, $endMonthName ${weekStart.year}"
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
            Icon(PhosphorIcons.Regular.CaretLeft, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
            Icon(PhosphorIcons.Regular.CaretRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Week day strip ───────────────────────────────────────────────────────────

@Composable
private fun WeekDayStrip(
    days: List<LocalDate>,
    today: LocalDate,
    selectedDay: LocalDate,
    habitsWithStatus: List<HabitWithStatus>,
    isCurrentWeek: Boolean,
    onDaySelect: (LocalDate) -> Unit
) {
    val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.forEachIndexed { index, day ->
            val isFuture = day > today
            val completionRatio = if (isCurrentWeek && !isFuture && habitsWithStatus.isNotEmpty()) {
                habitsWithStatus.count { it.weeklyCompletions.getOrNull(index) == true }.toFloat() / habitsWithStatus.size
            } else -1f
            WeekDayCell(
                letter = dayLetters[index],
                number = day.dayOfMonth,
                isToday = day == today,
                isSelected = day == selectedDay,
                isFuture = isFuture,
                completionRatio = completionRatio,
                onClick = { onDaySelect(day) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WeekDayCell(
    letter: String, number: Int,
    isToday: Boolean, isSelected: Boolean, isFuture: Boolean,
    completionRatio: Float,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val bg by animateColorAsState(
        when {
            isSelected && isToday -> primary
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else -> Color.Transparent
        }, tween(180), label = "dayBg"
    )
    val textColor = when {
        isSelected && isToday -> MaterialTheme.colorScheme.onPrimary
        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dotColor = when {
        completionRatio < 0 -> Color.Transparent
        completionRatio >= 0.8f -> Color(0xFF4CAF50)
        completionRatio >= 0.4f -> Color(0xFFF59E0B)
        completionRatio > 0 -> Color(0xFF6B7280)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(letter, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = if (isSelected || isToday) 1f else 0.65f), fontWeight = FontWeight.Medium)
        Text("$number", style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal, color = textColor)
        Box(Modifier.size(5.dp).clip(CircleShape).background(dotColor))
    }
}

// ─── Weekly intention card ────────────────────────────────────────────────────

@Composable
private fun WeekIntentionCard(intention: String, onUpdate: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(intention) { mutableStateOf(intention) }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.width(3.dp).height(42.dp).clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF7B61FF), Color(0xFF4FC3F7))))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "This week, I'm focused on",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = 0.3.sp
                )
                Spacer(Modifier.height(4.dp))
                if (editing) {
                    BasicTextField(
                        value = draft, onValueChange = { draft = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth(), maxLines = 3
                    )
                } else {
                    Text(
                        text = if (intention.isBlank()) "Tap to set your weekly intention…" else intention,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (intention.isBlank()) FontWeight.Normal else FontWeight.Medium,
                        color = if (intention.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().clickable { editing = true; draft = intention }
                    )
                }
            }
            if (editing) {
                TextButton(onClick = { onUpdate(draft); editing = false }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Research insight chip ────────────────────────────────────────────────────

@Composable
private fun ResearchInsightChip(insight: PlannerInsight) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(PhosphorIcons.Regular.Brain, null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            Text(
                "${insight.concept}  ·  ${insight.source}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

// ─── Energy block card ────────────────────────────────────────────────────────

@Composable
private fun EnergyBlockCard(
    block: EnergyBlock,
    habits: List<HabitWithStatus>,
    isToday: Boolean,
    dayIndexInWeek: Int,
    isCurrentWeek: Boolean,
    onCheckIn: (String) -> Unit
) {
    val blockIcon = when (block) {
        EnergyBlock.MORNING -> PhosphorIcons.Regular.Sun
        EnergyBlock.AFTERNOON -> PhosphorIcons.Regular.Sun
        EnergyBlock.EVENING -> PhosphorIcons.Regular.Moon
        EnergyBlock.ANYTIME -> PhosphorIcons.Regular.Sparkle
    }
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(blockIcon, null, modifier = Modifier.size(14.dp), tint = block.color)
                Text(block.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = block.color)
                Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Text(block.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Spacer(Modifier.weight(1f))
                Text(block.timeRange, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
            Spacer(Modifier.height(10.dp))
            habits.forEach { habitWithStatus ->
                val isCompleted = when {
                    isToday -> habitWithStatus.isCompletedToday
                    isCurrentWeek -> habitWithStatus.weeklyCompletions.getOrNull(dayIndexInWeek) ?: false
                    else -> false
                }
                val interactive = isToday
                PlannerHabitRow(
                    title = habitWithStatus.habit.title,
                    category = habitWithStatus.habit.category,
                    isCompleted = isCompleted,
                    isInteractive = interactive,
                    onToggle = { onCheckIn(habitWithStatus.habit.id) }
                )
            }
        }
    }
}

// ─── Habit row ────────────────────────────────────────────────────────────────

@Composable
private fun PlannerHabitRow(
    title: String,
    category: GoalCategory,
    isCompleted: Boolean,
    isInteractive: Boolean,
    onToggle: () -> Unit
) {
    val accent = categoryAccent(category)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(6.dp).clip(CircleShape)
                .background(if (isCompleted) accent else accent.copy(alpha = 0.3f))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (isInteractive) {
            Icon(
                imageVector = if (isCompleted) PhosphorIcons.Fill.CheckCircle else PhosphorIcons.Regular.Circle,
                contentDescription = if (isCompleted) "Done" else "Mark done",
                tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp).clickable(onClick = onToggle)
            )
        } else {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape)
                    .background(if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        }
    }
}

// ─── Weekly stats row ─────────────────────────────────────────────────────────

@Composable
private fun WeeklyStatsRow(
    completedToday: Int,
    totalHabits: Int,
    activeGoals: Int,
    topStreak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatPill("✅", "$completedToday/$totalHabits", "today", modifier = Modifier.weight(1f))
        StatPill("🎯", "$activeGoals", "active goals", modifier = Modifier.weight(1f))
        StatPill("🔥", "$topStreak", "top streak", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun PlannerEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.illus_empty_calendar),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text("No habits scheduled yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Add habits with reminder times to see your day plan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f), textAlign = TextAlign.Center)
    }
}
