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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.backgroundColor
import az.tribe.lifeplanner.ui.theme.gradientColors
import az.tribe.lifeplanner.ui.utils.formatHuman

@Composable
fun TodaysFocusSection(
    upcomingGoals: List<Goal>,
    onGoalClick: (Goal) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Today's Focus",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (upcomingGoals.isNotEmpty()) {
                Text(
                    "${upcomingGoals.size} upcoming",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (upcomingGoals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = LifePlannerDesign.Alpha.overlay)
                ),
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium)
            ) {
                Row(
                    modifier = Modifier.padding(LifePlannerDesign.Padding.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        PhosphorIcons.Regular.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "All caught up!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "No urgent deadlines. Keep up the great work!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                upcomingGoals.take(3).forEach { goal ->
                    CompactGoalCard(goal = goal, onClick = { onGoalClick(goal) })
                }
            }
        }
    }
}

@Composable
fun CompactGoalCard(
    goal: Goal,
    onClick: () -> Unit
) {
    val categoryColor = goal.category.backgroundColor()
    val categoryGradientColors = goal.category.gradientColors()
    val progress = (goal.progress ?: 0L).toInt()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = LifePlannerDesign.CornerRadius.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(brush = Brush.verticalGradient(categoryGradientColors))
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(LifePlannerDesign.Padding.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = categoryGradientColors.map { it.copy(alpha = 0.15f) }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(categoryGradientColors))
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Clock,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            goal.dueDate.formatHuman(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$progress%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryGradientColors.first()
                    )
                    Spacer(Modifier.height(4.dp))
                    GradientProgressBar(
                        progress = progress / 100f,
                        gradient = Brush.horizontalGradient(categoryGradientColors),
                        modifier = Modifier.width(56.dp),
                        height = 6.dp
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityGoalsSection(
    upcomingGoals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onSeeAllClick: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Priority Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (upcomingGoals.isNotEmpty() && onSeeAllClick != null) {
                Surface(
                    onClick = onSeeAllClick,
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "See all",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            PhosphorIcons.Regular.CaretRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (upcomingGoals.isNotEmpty()) {
                Text(
                    "${upcomingGoals.size} upcoming",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (upcomingGoals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = LifePlannerDesign.Alpha.overlay)
                ),
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium)
            ) {
                Row(
                    modifier = Modifier.padding(LifePlannerDesign.Padding.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        PhosphorIcons.Regular.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "All caught up!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "No urgent deadlines. Keep up the great work!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                upcomingGoals.take(5).forEach { goal ->
                    CompactGoalCard(goal = goal, onClick = { onGoalClick(goal) })
                }
            }
        }
    }
}

@Composable
fun CompactGoalTile(
    goal: Goal,
    onClick: () -> Unit
) {
    val categoryGradientColors = goal.category.gradientColors()
    val categoryColor = goal.category.backgroundColor()
    val progress = (goal.progress ?: 0L).toInt()

    GlassCard(
        modifier = Modifier
            .widthIn(min = 140.dp)
            .width(140.dp)
            .clickable(onClick = onClick),
        cornerRadius = LifePlannerDesign.CornerRadius.medium
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Brush.horizontalGradient(categoryGradientColors))
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                categoryGradientColors.map { it.copy(alpha = 0.15f) }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = goal.category.getIcon(),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    goal.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GradientProgressBar(
                        progress = progress / 100f,
                        gradient = Brush.horizontalGradient(categoryGradientColors),
                        modifier = Modifier.weight(1f),
                        height = 4.dp
                    )
                    Text(
                        "$progress%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryGradientColors.first()
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    goal.dueDate.formatHuman(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Vertical row-style goal item for the Home screen.
 * Left: category icon badge. Middle: title + due date + progress bar. Right: % + chevron.
 */
@Composable
fun CompactGoalRow(
    goal: Goal,
    onClick: () -> Unit
) {
    val categoryGradientColors = goal.category.gradientColors()
    val categoryColor = goal.category.backgroundColor()
    val progress = (goal.progress ?: 0L).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(categoryGradientColors.map { it.copy(alpha = 0.15f) })
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = goal.category.getIcon(),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.dueDate.formatHuman(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = goal.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
            }
            GradientProgressBar(
                progress = progress / 100f,
                gradient = Brush.horizontalGradient(categoryGradientColors),
                modifier = Modifier.fillMaxWidth(),
                height = 3.dp
            )
        }

        Text(
            text = "$progress%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = categoryGradientColors.first()
        )

        Icon(
            imageVector = PhosphorIcons.Regular.CaretRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}
