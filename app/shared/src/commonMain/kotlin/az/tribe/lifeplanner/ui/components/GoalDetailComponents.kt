package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Hourglass
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.GoalPractice
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.backgroundColor
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * The goal written like a document rather than announced like a poster.
 *
 * Replaces the hero header: a full-bleed category gradient, a 140dp progress ring, and the title
 * in bold white on top of it. The title was the one thing on that banner that mattered and the one
 * thing that suffered; a real goal name clipped or wrapped into a shout. Paper rules instead: the
 * category is a quiet overline, the title is text that wraps as long as it needs to, and progress
 * is a thin line with the numbers written beside it.
 */
@Composable
fun GoalPaperHeader(
    goal: Goal,
    /** Non-null when habits are linked, which makes this a practice rather than a checklist. */
    practice: GoalPractice? = null,
    modifier: Modifier = Modifier
) {
    val accent = goal.category.backgroundColor()
    val progress = (goal.progress?.toFloat() ?: 0f) / 100f
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysLeft = goal.dueDate.toEpochDays() - today.toEpochDays()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = goal.category.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = accent
        )

        Text(
            text = goal.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (progress > 0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
        }

        val due = when {
            daysLeft == 0L -> "due today"
            daysLeft < 0L -> "was due ${formatShortDate(goal.dueDate)}"
            else -> "due ${formatShortDate(goal.dueDate)}"
        }
        Text(
            text = when {
                practice != null && practice.isEstablished ->
                    "Day ${practice.dayNumber} · $due"
                practice != null ->
                    "Day ${practice.dayNumber} of ${practice.windowDays} · $due"
                goal.milestones.isNotEmpty() ->
                    "${goal.milestones.count { it.isCompleted }} of ${goal.milestones.size} steps · $due"
                else -> due.replaceFirstChar { it.uppercase() }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Inline status toggle buttons
 */
@Composable
fun StatusToggleButtons(
    currentStatus: GoalStatus,
    onStatusChange: (GoalStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusToggleButton.entries.forEach { status ->
                val isSelected = currentStatus == status.goalStatus

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) status.activeColor else Color.Transparent,
                    animationSpec = tween(300),
                    label = "statusBg"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(300),
                    label = "statusContent"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChange(status.goalStatus) },
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = status.icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

private enum class StatusToggleButton(
    val goalStatus: GoalStatus,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val activeColor: Color
) {
    NOT_STARTED(
        GoalStatus.NOT_STARTED,
        "Not Started",
        PhosphorIcons.Regular.Hourglass,
        Color(0xFF9E9E9E)
    ),
    IN_PROGRESS(
        GoalStatus.IN_PROGRESS,
        "In Progress",
        PhosphorIcons.Regular.Play,
        Color(0xFFFFA726)
    ),
    COMPLETED(
        GoalStatus.COMPLETED,
        "Completed",
        PhosphorIcons.Regular.CheckCircle,
        Color(0xFF66BB6A)
    )
}

/**
 * Modern section header with icon
 */
@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        trailing?.invoke()
    }
}

private fun formatShortDate(date: LocalDate): String {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${months[date.month.number - 1]} ${date.day}"
}
