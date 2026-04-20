package az.tribe.lifeplanner.ui.retrospective

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.Warning
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.FocusSession
import az.tribe.lifeplanner.domain.model.HabitDayStatus
import az.tribe.lifeplanner.domain.model.HabitDaySummary
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.getIcon
import az.tribe.lifeplanner.ui.theme.backgroundColor

@Composable
internal fun JournalSection(entries: List<JournalEntry>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        entry.mood.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            entry.content.take(80) + if (entry.content.length > 80) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (index < entries.size - 1) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

private const val HABITS_COLLAPSE_THRESHOLD = 5
private const val HABITS_NUDGE_THRESHOLD = 7

@Composable
internal fun HabitsSection(summary: HabitDaySummary) {
    var expanded by remember { mutableStateOf(false) }
    val habits = summary.habits
    val showToggle = habits.size > HABITS_COLLAPSE_THRESHOLD
    val visibleHabits = if (expanded || !showToggle) habits else habits.take(HABITS_COLLAPSE_THRESHOLD)
    val hiddenCount = habits.size - HABITS_COLLAPSE_THRESHOLD

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            visibleHabits.forEachIndexed { index, habit ->
                HabitDayRow(habit)
                if (index < visibleHabits.size - 1 || showToggle || habits.size > HABITS_NUDGE_THRESHOLD) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    )
                }
            }

            // Expand / collapse toggle
            if (showToggle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (expanded) PhosphorIcons.Regular.CaretUp else PhosphorIcons.Regular.CaretDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (expanded) "Show less" else "Show $hiddenCount more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Too-many-habits nudge
            if (habits.size > HABITS_NUDGE_THRESHOLD) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        PhosphorIcons.Regular.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "More than $HABITS_NUDGE_THRESHOLD habits is hard to track consistently",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun HabitDayRow(habit: HabitDayStatus) {
    val categoryColor = habit.category.backgroundColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = habit.category.getIcon(),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Title
        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Status indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (habit.wasCompleted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (habit.wasCompleted) PhosphorIcons.Regular.Check else PhosphorIcons.Regular.X,
                contentDescription = if (habit.wasCompleted) "Completed" else "Missed",
                tint = if (habit.wasCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
internal fun FocusSection(sessions: List<FocusSession>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sessions.forEachIndexed { index, session ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF6B35).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${session.actualMinutes}m" + if (session.wasCompleted) " (completed)" else " (partial)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${session.plannedDurationMinutes}m planned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session.xpEarned > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFFA726).copy(alpha = 0.12f)
                        ) {
                            Text(
                                "+${session.xpEarned} XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFA726),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                if (index < sessions.size - 1) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
