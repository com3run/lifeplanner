package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.utils.formatHuman
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Note
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Warning

@Composable
internal fun CompletedGoalBanner(
    milestonesCompleted: Int,
    totalMilestones: Int,
    reflectionsCount: Int,
    primaryColor: Color,
    onReopenGoal: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.18f),
                            primaryColor.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\uD83C\uDFC6",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Goal Achieved!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "You completed this goal. Here's a look at your journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            primaryColor.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (totalMilestones > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$milestonesCompleted/$totalMilestones",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "Milestones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$reflectionsCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "Reflections",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = onReopenGoal) {
                    Text(
                        text = "Reopen Goal",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun GoalNotFoundState(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Goal not found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledTonalButton(onClick = onBackClick) {
                Text("Go Back")
            }
        }
    }
}

@Composable
internal fun ModernNotesCard(
    notes: String,
    isReadOnly: Boolean = false,
    onNotesClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(if (!isReadOnly) Modifier.clickable { onNotesClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(LifePlannerDesign.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Note,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isReadOnly) {
                    IconButton(
                        onClick = onNotesClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            PhosphorIcons.Regular.PencilSimple,
                            contentDescription = "Edit Notes",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = if (notes.isBlank()) {
                    if (isReadOnly) "No notes were added." else "Tap to add notes..."
                } else notes,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notes.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = if (isReadOnly) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AiReasoningCard(reasoning: String) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(LifePlannerDesign.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Why this goal?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF7C4DFF).copy(alpha = 0.1f)
                ) {
                    Text(
                        "AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C4DFF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ModernMilestonesCard(
    milestones: List<Milestone>,
    isReadOnly: Boolean = false,
    onMilestoneToggle: (String) -> Unit,
    onAddMilestone: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(LifePlannerDesign.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isReadOnly) "What You Achieved" else "Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${milestones.count { it.isCompleted }}/${milestones.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (!isReadOnly) {
                    IconButton(
                        onClick = onAddMilestone,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Plus,
                            contentDescription = "Add Milestone",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            milestones.forEach { milestone ->
                MilestoneItem(
                    milestone = milestone,
                    isReadOnly = isReadOnly,
                    onClick = { onMilestoneToggle(milestone.id) }
                )
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: Milestone,
    isReadOnly: Boolean = false,
    onClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (!isReadOnly) Modifier.clickable {
                    haptic.click()
                    onClick()
                } else Modifier
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (milestone.isCompleted)
                PhosphorIcons.Fill.CheckCircle
            else
                PhosphorIcons.Regular.Circle,
            contentDescription = null,
            tint = if (milestone.isCompleted)
                Color(0xFF66BB6A)
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            milestone.dueDate?.let { date ->
                Text(
                    text = date.formatHuman(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun EmptyMilestonesCard(
    onAddMilestone: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                PhosphorIcons.Regular.Flag,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Text(
                text = "No milestones yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Break down your goal into smaller milestones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onAddMilestone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    PhosphorIcons.Regular.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Milestone")
            }
        }
    }
}
