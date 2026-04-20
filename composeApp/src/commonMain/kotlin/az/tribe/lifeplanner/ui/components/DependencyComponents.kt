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
import com.adamglin.phosphoricons.regular.ArrowBendDownRight
import com.adamglin.phosphoricons.regular.Lifebuoy
import com.adamglin.phosphoricons.regular.Link
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Prohibit
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.TreeStructure
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.DependencyType
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.GoalDependency

/**
 * Card showing goal dependencies in GoalDetailScreen
 */
@Composable
fun DependenciesCard(
    dependencies: List<GoalDependency>,
    goals: List<Goal>,
    currentGoalId: String,
    suggestedDependencies: List<Pair<Goal, DependencyType>>,
    onAddDependency: () -> Unit,
    onRemoveDependency: (String) -> Unit,
    onViewDependencyGraph: () -> Unit,
    onGoalClick: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Linked Goals",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (dependencies.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${dependencies.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.clickable(onClick = onAddDependency),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Plus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Link",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (dependencies.isEmpty()) {
                // Compact empty state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No linked goals yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Dependency list
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    dependencies.forEach { dependency ->
                        val linkedGoalId = if (dependency.sourceGoalId == currentGoalId)
                            dependency.targetGoalId else dependency.sourceGoalId
                        val linkedGoal = goals.find { it.id == linkedGoalId }

                        linkedGoal?.let { goal ->
                            DependencyItem(
                                goal = goal,
                                dependencyType = dependency.dependencyType,
                                isSource = dependency.sourceGoalId == currentGoalId,
                                onGoalClick = { onGoalClick(goal.id) },
                                onRemove = { onRemoveDependency(dependency.id) }
                            )
                        }
                    }
                }

                // See map link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "See dependency map →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.clickable(onClick = onViewDependencyGraph)
                    )
                }
            }
        }
    }
}

@Composable
fun DependencyItem(
    goal: Goal,
    dependencyType: DependencyType,
    isSource: Boolean,
    onGoalClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable { onGoalClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                text = dependencyType.simpleLabel(isSource),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Icon(
            PhosphorIcons.Regular.X,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
fun SuggestedDependencyItem(
    goal: Goal,
    suggestedType: DependencyType,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = suggestedType.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = goal.title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = suggestedType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Dialog for confirming dependency removal
 */
@Composable
fun RemoveDependencyDialog(
    isVisible: Boolean,
    goalTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                PhosphorIcons.Regular.Trash,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Remove Dependency")
        },
        text = {
            Text("Remove the link to \"$goalTitle\"? This won't delete the goal itself.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun DependencyType.simpleLabel(isSource: Boolean): String = when (this) {
    DependencyType.BLOCKS -> if (isSource) "blocks" else "blocked by"
    DependencyType.BLOCKED_BY -> if (isSource) "blocked by" else "blocks"
    DependencyType.RELATED -> "related"
    DependencyType.PARENT_OF -> if (isSource) "parent" else "child"
    DependencyType.CHILD_OF -> if (isSource) "child" else "parent"
    DependencyType.SUPPORTS -> "supports"
}

// Extension functions for DependencyType
fun DependencyType.icon(): ImageVector = when (this) {
    DependencyType.BLOCKS -> PhosphorIcons.Regular.Prohibit
    DependencyType.BLOCKED_BY -> PhosphorIcons.Regular.Prohibit
    DependencyType.RELATED -> PhosphorIcons.Regular.Link
    DependencyType.PARENT_OF -> PhosphorIcons.Regular.TreeStructure
    DependencyType.CHILD_OF -> PhosphorIcons.Regular.ArrowBendDownRight
    DependencyType.SUPPORTS -> PhosphorIcons.Regular.Lifebuoy
}

fun DependencyType.color(): Color = when (this) {
    DependencyType.BLOCKS -> Color(0xFFE57373)
    DependencyType.BLOCKED_BY -> Color(0xFFE57373)
    DependencyType.RELATED -> Color(0xFF64B5F6)
    DependencyType.PARENT_OF -> Color(0xFF81C784)
    DependencyType.CHILD_OF -> Color(0xFF81C784)
    DependencyType.SUPPORTS -> Color(0xFFFFB74D)
}

fun DependencyType.getInverseType(): DependencyType = when (this) {
    DependencyType.BLOCKS -> DependencyType.BLOCKED_BY
    DependencyType.BLOCKED_BY -> DependencyType.BLOCKS
    DependencyType.PARENT_OF -> DependencyType.CHILD_OF
    DependencyType.CHILD_OF -> DependencyType.PARENT_OF
    DependencyType.RELATED -> DependencyType.RELATED
    DependencyType.SUPPORTS -> DependencyType.SUPPORTS
}
