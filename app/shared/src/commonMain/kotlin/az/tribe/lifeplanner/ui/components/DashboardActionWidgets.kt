package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Fire
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.habit.HabitWithStatus
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

// ============== NEXT ACTION MODEL ==============

sealed class NextAction {
    data class GoalDueToday(val goal: Goal) : NextAction()
    data class NextHabit(val habitWithStatus: HabitWithStatus) : NextAction()
    data class ContinueGoal(val goal: Goal) : NextAction()
    object AllCaughtUp : NextAction()
}

@Composable
fun NextActionCard(
    nextAction: NextAction,
    onGoalClick: (Goal) -> Unit,
    // Tapping the card opens the habit's detail; it does not check it in. Completing from a
    // glanceable card is too easy to do by accident, and the card is meant to point you at the
    // thing, not silently mark it done.
    onHabitClick: (String) -> Unit
) {
    val isHabitDone = nextAction is NextAction.NextHabit &&
            nextAction.habitWithStatus.isCompletedToday

    val greenColors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))

    val (icon, title, subtitle, progress, gradientColors) = when (nextAction) {
        is NextAction.GoalDueToday -> {
            val goal = nextAction.goal
            val p = (goal.progress ?: 0L).toInt()
            ActionCardData(
                icon = PhosphorIcons.Regular.Flag,
                title = goal.title,
                subtitle = "Due today \u2022 ${p}% complete",
                progress = p / 100f,
                gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8F65))
            )
        }
        is NextAction.NextHabit -> {
            val habit = nextAction.habitWithStatus.habit
            ActionCardData(
                icon = if (isHabitDone) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Repeat,
                title = habit.title,
                subtitle = if (isHabitDone) "Done today \u2714" else "Next habit to check in",
                progress = null,
                gradientColors = greenColors
            )
        }
        is NextAction.ContinueGoal -> {
            val goal = nextAction.goal
            val p = (goal.progress ?: 0L).toInt()
            ActionCardData(
                icon = PhosphorIcons.Regular.TrendUp,
                title = goal.title,
                subtitle = "Continue \u2022 ${p}% complete",
                progress = p / 100f,
                gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            )
        }
        is NextAction.AllCaughtUp -> {
            ActionCardData(
                icon = PhosphorIcons.Regular.CheckCircle,
                title = "All caught up!",
                subtitle = "Great work \u2014 enjoy your day",
                progress = null,
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            )
        }
    }

    GradientBorderCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.large))
            .clickable {
                when (nextAction) {
                    is NextAction.GoalDueToday -> onGoalClick(nextAction.goal)
                    is NextAction.ContinueGoal -> onGoalClick(nextAction.goal)
                    is NextAction.NextHabit -> onHabitClick(nextAction.habitWithStatus.habit.id)
                    is NextAction.AllCaughtUp -> {}
                }
            },
        gradientColors = gradientColors,
        borderWidth = 1.5.dp,
        cornerRadius = LifePlannerDesign.CornerRadius.large,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isHabitDone) Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.08f),
                                Color(0xFF4CAF50).copy(alpha = 0.03f)
                            )
                        )
                    ) else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isHabitDone) "Habit" else "Up Next",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = gradientColors.first()
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isHabitDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isHabitDone)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHabitDone)
                        Color(0xFF4CAF50).copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    GradientProgressBar(
                        progress = progress,
                        gradient = Brush.horizontalGradient(gradientColors),
                        modifier = Modifier.fillMaxWidth(),
                        height = 4.dp
                    )
                }
            }

            if (!isHabitDone && nextAction !is NextAction.AllCaughtUp) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    PhosphorIcons.Regular.ArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class ActionCardData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val progress: Float?,
    val gradientColors: List<Color>
)

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useGradient: Boolean = false,
    gradientColors: List<Color>? = null
) {
    val backgroundModifier = if (useGradient && gradientColors != null) {
        Modifier.background(
            brush = Brush.horizontalGradient(gradientColors),
            shape = RoundedCornerShape(50)
        )
    } else {
        Modifier.background(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(50)
        )
    }

    val contentColor = if (useGradient) Color.White else color

    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(backgroundModifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAddGoalClick: () -> Unit,
    onAiSuggestClick: () -> Unit,
    onNewHabitClick: () -> Unit,
    onJournalClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = PhosphorIcons.Regular.Plus,
                label = "Add Goal",
                color = MaterialTheme.colorScheme.primary,
                onClick = onAddGoalClick,
                modifier = Modifier.weight(1f),
                useGradient = true,
                gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            )
            QuickActionCard(
                icon = PhosphorIcons.Regular.Sparkle,
                label = "AI Suggest",
                color = MaterialTheme.colorScheme.secondary,
                onClick = onAiSuggestClick,
                modifier = Modifier.weight(1f),
                useGradient = true,
                gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = PhosphorIcons.Regular.Repeat,
                label = "New Habit",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onNewHabitClick,
                modifier = Modifier.weight(1f),
                useGradient = true,
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            )
            QuickActionCard(
                icon = PhosphorIcons.Regular.PencilSimple,
                label = "Journal",
                color = MaterialTheme.colorScheme.secondary,
                onClick = onJournalClick,
                modifier = Modifier.weight(1f),
                useGradient = true,
                gradientColors = listOf(Color(0xFFFC466B), Color(0xFF3F5EFB))
            )
        }
    }
}

/**
 * Contextual smart actions, shows up to 4 actions that change based on user state.
 * Prioritizes what matters most right now.
 */
@Composable
fun QuickActionsPillRow(
    onAddGoal: () -> Unit,
    onAiSuggest: () -> Unit,
    onNewHabit: () -> Unit,
    onHabitCheckIn: () -> Unit = onNewHabit,
    onJournal: () -> Unit,
    onFocus: () -> Unit = {},
    onCoach: () -> Unit = {},
    isCoachLocked: Boolean = true,
    hasGoals: Boolean = false,
    hasHabits: Boolean = false,
    pendingHabits: Int = 0,
    streak: Int = 0,
    goalsDueToday: Int = 0
) {
    data class SmartAction(
        val icon: ImageVector,
        val label: String,
        val subtitle: String,
        val gradientColors: List<Color>,
        val onClick: () -> Unit,
        val isLocked: Boolean = false,
        val priority: Int = 0
    )

    val actions = remember(hasGoals, hasHabits, pendingHabits, streak, goalsDueToday, isCoachLocked) {
        buildList {
            if (goalsDueToday > 0) {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Clock,
                    label = "Due Today",
                    subtitle = "$goalsDueToday goal${if (goalsDueToday > 1) "s" else ""} due",
                    gradientColors = listOf(Color(0xFFFF5252), Color(0xFFFF1744)),
                    onClick = onAddGoal,
                    priority = 0
                ))
            }
            if (pendingHabits > 0) {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Check,
                    label = "Check In",
                    subtitle = "$pendingHabits habit${if (pendingHabits > 1) "s" else ""} left",
                    gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                    onClick = onHabitCheckIn,
                    priority = 1
                ))
            }
            if (streak > 0) {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Fire,
                    label = "Keep Streak",
                    subtitle = "$streak day${if (streak > 1) "s" else ""} strong",
                    gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFFA726)),
                    onClick = onHabitCheckIn,
                    priority = 2
                ))
            }
            if (!hasGoals) {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Flag,
                    label = "First Goal",
                    subtitle = "Start planning",
                    gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                    onClick = onAddGoal,
                    priority = 3
                ))
            } else {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Plus,
                    label = "New Goal",
                    subtitle = "Add a goal",
                    gradientColors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
                    onClick = onAddGoal,
                    priority = 5
                ))
            }
            if (!hasHabits) {
                add(SmartAction(
                    icon = PhosphorIcons.Regular.Repeat,
                    label = "First Habit",
                    subtitle = "Build consistency",
                    gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                    onClick = onNewHabit,
                    priority = 4
                ))
            }
            add(SmartAction(
                icon = PhosphorIcons.Regular.PencilSimple,
                label = "Journal",
                subtitle = "Reflect today",
                gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
                onClick = onJournal,
                priority = 6
            ))
            add(SmartAction(
                icon = PhosphorIcons.Regular.Timer,
                label = "Focus",
                subtitle = "Deep work",
                gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFFA726)),
                onClick = onFocus,
                priority = 7
            ))
            add(SmartAction(
                icon = PhosphorIcons.Regular.Sparkle,
                label = "AI Goals",
                subtitle = "Get suggestions",
                gradientColors = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
                onClick = onAiSuggest,
                priority = 8
            ))
            add(SmartAction(
                icon = PhosphorIcons.Regular.Brain,
                label = "Coach",
                subtitle = if (isCoachLocked) "Lv.3 to unlock" else "Get guidance",
                gradientColors = if (isCoachLocked) listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD))
                else listOf(Color(0xFF7C4DFF), Color(0xFF00BFA5)),
                onClick = onCoach,
                isLocked = isCoachLocked,
                priority = 9
            ))
        }
            .sortedBy { it.priority }
            .distinctBy { it.label }
            .take(4)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            SmartActionCard(
                icon = action.icon,
                label = action.label,
                subtitle = action.subtitle,
                gradientColors = action.gradientColors,
                onClick = action.onClick,
                isLocked = action.isLocked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmartActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        cornerRadius = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else gradientColors.first().copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
