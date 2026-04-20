package az.tribe.lifeplanner.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.FocusTheme
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.gradientColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Infinity
import com.adamglin.phosphoricons.regular.Minus
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Timer

@Composable
internal fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun MoodChip(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.15f) else Color.Transparent,
        modifier = if (isSelected) {
            Modifier.border(2.dp, Color(0xFFFF6B35), CircleShape)
        } else {
            Modifier
        }
    ) {
        Text(
            mood.emoji,
            modifier = Modifier.padding(12.dp),
            fontSize = 24.sp
        )
    }
}

@Composable
internal fun ThemePreviewCard(
    theme: FocusTheme,
    mood: Mood?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        FocusAnimatedBackground(
            theme = theme,
            mood = mood,
            modifier = Modifier.fillMaxSize()
        )
        // Label overlay
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.3f)
        ) {
            Text(
                "${theme.icon} ${theme.displayName}",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
internal fun PickerChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) Color(0xFFFF6B35) else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
internal fun GoalSectionHeader(goal: Goal) {
    val categoryColors = goal.category.gradientColors()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(categoryColors))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            goal.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun MilestonePickerItem(
    milestoneItem: MilestoneItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColors = milestoneItem.goal.category.gradientColors()
    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.horizontalGradient(categoryColors),
            shape = RoundedCornerShape(16.dp)
        )
    } else Modifier

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    milestoneItem.milestone.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    PhosphorIcons.Regular.Check,
                    null,
                    tint = categoryColors.first(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun DurationChip(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) Color(0xFFFF6B35) else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            "${minutes}m",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
internal fun TimerModeToggle(
    isFreeFlow: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Timed segment
            Surface(
                onClick = { onModeChange(false) },
                shape = RoundedCornerShape(12.dp),
                color = if (!isFreeFlow) Color(0xFFFF6B35) else Color.Transparent,
                contentColor = if (!isFreeFlow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PhosphorIcons.Regular.Timer, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Timed",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Free Flow segment
            Surface(
                onClick = { onModeChange(true) },
                shape = RoundedCornerShape(12.dp),
                color = if (isFreeFlow) Color(0xFFFF6B35) else Color.Transparent,
                contentColor = if (isFreeFlow) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PhosphorIcons.Regular.Infinity, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Free Flow",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun CustomDurationStepper(
    minutes: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onDecrement,
            enabled = minutes > 5,
            shape = CircleShape,
            modifier = Modifier.size(44.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(PhosphorIcons.Regular.Minus, "Decrease", modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(24.dp))
        Text(
            "$minutes min",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )
        Spacer(Modifier.width(24.dp))
        FilledTonalButton(
            onClick = onIncrement,
            enabled = minutes < 120,
            shape = CircleShape,
            modifier = Modifier.size(44.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(PhosphorIcons.Regular.Plus, "Increase", modifier = Modifier.size(20.dp))
        }
    }
}

internal fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m" else "${seconds}s"
}
