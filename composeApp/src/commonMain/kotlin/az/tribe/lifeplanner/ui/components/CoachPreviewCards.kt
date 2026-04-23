package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachSuggestion
import az.tribe.lifeplanner.ui.theme.CategoryColors

@Composable
internal fun GoalPreviewCard(
    suggestion: CoachSuggestion.CreateGoal,
    onAdd: () -> Unit,
    isExecuting: Boolean,
    isAdded: Boolean = false
) {
    val category = try {
        GoalCategory.valueOf(suggestion.category)
    } catch (e: Exception) {
        GoalCategory.CAREER
    }

    val categoryColor = category.getPreviewColor()
    val gradient = category.getGradient()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(gradient))

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = PhosphorIcons.Regular.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(text = "New Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = category.displayName, style = MaterialTheme.typography.labelMedium, color = categoryColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    TimelineChip(timeline = suggestion.timeline)
                }

                Text(text = suggestion.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (suggestion.description.isNotBlank()) {
                    Text(text = suggestion.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                if (suggestion.milestones.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().background(categoryColor.copy(alpha = 0.06f), RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Milestones", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = categoryColor)
                        suggestion.milestones.forEachIndexed { index, milestone ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier.size(22.dp).background(categoryColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${index + 1}", style = MaterialTheme.typography.labelSmall, color = categoryColor, fontWeight = FontWeight.Bold)
                                }
                                Text(text = milestone.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                GradientActionButton(text = if (isAdded) "Added" else "Add Goal", gradient = gradient, onClick = onAdd, isExecuting = isExecuting, isAdded = isAdded)
            }
        }
    }
}

@Composable
internal fun HabitPreviewCard(
    suggestion: CoachSuggestion.CreateHabit,
    onAdd: () -> Unit,
    isExecuting: Boolean,
    isAdded: Boolean = false
) {
    val category = try {
        GoalCategory.valueOf(suggestion.category)
    } catch (e: Exception) {
        GoalCategory.BODY
    }

    val categoryColor = category.getPreviewColor()
    val gradient = category.getGradient()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(gradient))

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = PhosphorIcons.Regular.Repeat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(text = "New Habit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = category.displayName, style = MaterialTheme.typography.labelMedium, color = categoryColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    FrequencyBadge(frequency = suggestion.frequency)
                }

                Text(text = suggestion.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (suggestion.description.isNotBlank()) {
                    Text(text = suggestion.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                GradientActionButton(text = if (isAdded) "Added" else "Add Habit", gradient = gradient, onClick = onAdd, isExecuting = isExecuting, isAdded = isAdded)
            }
        }
    }
}

@Composable
internal fun GradientActionButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    isExecuting: Boolean,
    isAdded: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Surface(
        onClick = onClick,
        enabled = !isExecuting && !isAdded,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isAdded) Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary)) else gradient,
                    RoundedCornerShape(14.dp)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isExecuting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else if (isAdded) {
                    Icon(imageVector = PhosphorIcons.Regular.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Text(text = text, fontWeight = FontWeight.SemiBold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(imageVector = PhosphorIcons.Regular.Plus, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Text(text = text, fontWeight = FontWeight.SemiBold, color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
internal fun TimelineChip(timeline: String) {
    val (text, color) = when (timeline) {
        "SHORT_TERM" -> "30 days" to Color(0xFF4CAF50)
        "MID_TERM" -> "90 days" to Color(0xFFFF9800)
        "LONG_TERM" -> "1 year" to Color(0xFF2196F3)
        else -> "90 days" to Color(0xFFFF9800)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.height(26.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = PhosphorIcons.Regular.Clock, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun FrequencyBadge(frequency: String) {
    val (text, color) = when (frequency.uppercase()) {
        "DAILY" -> "Daily" to Color(0xFF4CAF50)
        "WEEKLY" -> "Weekly" to Color(0xFF2196F3)
        else -> "Daily" to Color(0xFF4CAF50)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.height(26.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = PhosphorIcons.Regular.Repeat, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun GoalCategory.getPreviewColor(): Color = when (this) {
    GoalCategory.CAREER -> CategoryColors.CAREER
    GoalCategory.MONEY -> CategoryColors.MONEY
    GoalCategory.BODY -> CategoryColors.BODY
    GoalCategory.PEOPLE -> CategoryColors.PEOPLE
    GoalCategory.WELLBEING -> CategoryColors.WELLBEING
    GoalCategory.PURPOSE -> CategoryColors.PURPOSE
    GoalCategory.FAMILY -> CategoryColors.FAMILY
}

private fun GoalCategory.getGradient(): Brush {
    val colors = when (this) {
        GoalCategory.CAREER -> CategoryColors.CAREER_GRADIENT
        GoalCategory.MONEY -> CategoryColors.MONEY_GRADIENT
        GoalCategory.BODY -> CategoryColors.BODY_GRADIENT
        GoalCategory.PEOPLE -> CategoryColors.PEOPLE_GRADIENT
        GoalCategory.WELLBEING -> CategoryColors.WELLBEING_GRADIENT
        GoalCategory.PURPOSE -> CategoryColors.PURPOSE_GRADIENT
        GoalCategory.FAMILY -> CategoryColors.FAMILY_GRADIENT
    }
    return Brush.horizontalGradient(colors)
}

private val GoalCategory.displayName: String
    get() = when (this) {
        GoalCategory.CAREER -> "Career"
        GoalCategory.MONEY -> "Money"
        GoalCategory.BODY -> "Body"
        GoalCategory.PEOPLE -> "People"
        GoalCategory.WELLBEING -> "Wellbeing"
        GoalCategory.PURPOSE -> "Purpose"
        GoalCategory.FAMILY -> "Family"
    }
