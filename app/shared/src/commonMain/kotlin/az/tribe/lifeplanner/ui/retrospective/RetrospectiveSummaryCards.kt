package az.tribe.lifeplanner.ui.retrospective

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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.ListChecks
import com.adamglin.phosphoricons.regular.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.DaySnapshot
import az.tribe.lifeplanner.ui.components.GlassCard

@Composable
internal fun DaySummaryCard(snapshot: DaySnapshot) {
    val allHabitsComplete = snapshot.habitSummary.completedHabits == snapshot.habitSummary.totalHabits
            && snapshot.habitSummary.totalHabits > 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                icon = PhosphorIcons.Regular.Heart,
                iconTint = Color(0xFFE91E63),
                label = snapshot.dominantMood?.displayName ?: "No mood"
            )
            SummaryItem(
                icon = PhosphorIcons.Regular.ListChecks,
                iconTint = if (allHabitsComplete) Color(0xFF4CAF50) else Color(0xFFFFA726),
                label = "${snapshot.habitSummary.completedHabits}/${snapshot.habitSummary.totalHabits} habits"
            )
            SummaryItem(
                icon = PhosphorIcons.Regular.Timer,
                iconTint = Color(0xFFFF6B35),
                label = "${snapshot.totalFocusMinutes}m focus"
            )
            SummaryItem(
                icon = PhosphorIcons.Regular.ArrowsClockwise,
                iconTint = Color(0xFF764BA2),
                label = "${snapshot.goalChanges.size} changes"
            )
        }
    }
}

@Composable
internal fun SummaryItem(
    icon: ImageVector,
    iconTint: Color,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}
