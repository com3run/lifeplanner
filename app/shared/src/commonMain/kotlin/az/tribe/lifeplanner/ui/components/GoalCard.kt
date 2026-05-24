package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ListChecks
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Hourglass
import com.adamglin.phosphoricons.regular.Play
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.utils.formatHuman
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun GoalCard(
    goal: Goal,
    modifier: Modifier = Modifier
) {
    // Check if goal was created today
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val isNew = goal.createdAt.date == today

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title, full width so even long titles can display 2 lines
        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Status badges below title
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isNew) NewBadge()
            StatusChip(status = goal.status)
        }

        // Description, 1 line; full text always accessible on detail screen
        if (goal.description.isNotBlank()) {
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Progress indicator with animation
        if (goal.progress != null && goal.progress > 0) {
            ProgressSection(
                progress = goal.progress.toInt(),
                color = goal.category.backgroundColor()
            )
        }

        // Footer with metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Due date with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.CalendarBlank,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = goal.dueDate.formatHuman(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }

            // Milestones indicator
            if (goal.milestones.isNotEmpty()) {
                MilestoneIndicator(
                    completed = goal.milestones.count { it.isCompleted },
                    total = goal.milestones.size
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    progress: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Animated progress percentage
            val animatedProgress by animateIntAsState(
                targetValue = progress,
                animationSpec = tween(800, easing = FastOutSlowInEasing),
                label = "progress"
            )

            Text(
                text = "$animatedProgress%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Modern progress bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            // Animated progress fill
            val animatedWidth by animateFloatAsState(
                targetValue = progress / 100f,
                animationSpec = tween(800, easing = FastOutSlowInEasing),
                label = "width"
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun MilestoneIndicator(
    completed: Int,
    total: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.ListChecks,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )

        // Milestone dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < completed)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun NewBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF4CAF50),
        modifier = Modifier.height(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Sparkle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "NEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun StatusChip(status: GoalStatus) {
    val (icon, text, colors) = when (status) {
        GoalStatus.NOT_STARTED -> Triple(
            PhosphorIcons.Regular.Hourglass,
            "Not Started",
            ChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        GoalStatus.IN_PROGRESS -> Triple(
            PhosphorIcons.Regular.Play,
            "In Progress",
            ChipColors(
                containerColor = Color(0xFFFFF8E1),
                contentColor = Color(0xFFFF8F00)
            )
        )
        GoalStatus.COMPLETED -> Triple(
            PhosphorIcons.Regular.CheckCircle,
            "Completed",
            ChipColors(
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32)
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.containerColor,
        modifier = Modifier.height(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.contentColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = colors.contentColor
            )
        }
    }
}

private data class ChipColors(
    val containerColor: Color,
    val contentColor: Color
)

// Extension function to get category background color with more vibrant colors
fun GoalCategory.backgroundColor(): Color {
    return when (this) {
        GoalCategory.CAREER -> Color(0xFF2196F3)
        GoalCategory.MONEY -> Color(0xFF4CAF50)
        GoalCategory.BODY -> Color(0xFFFF9800)
        GoalCategory.PEOPLE -> Color(0xFF9C27B0)
        GoalCategory.WELLBEING -> Color(0xFF009688)
        GoalCategory.PURPOSE -> Color(0xFFE91E63)
        GoalCategory.FAMILY -> Color(0xFFF57C00)
    }
}

