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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Sparkle

internal data class SuggestedHabit(
    val title: String,
    val description: String,
    val frequency: HabitFrequency,
    val emoji: String
)

@Composable
internal fun HabitsStep(
    habits: List<Pair<SuggestedHabit, Boolean>>,
    onToggle: (Int) -> Unit,
    onCreateWithHabits: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Build daily momentum",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "These habits will keep you moving toward your goal every day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (habits.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(14.dp))
                    Text(
                        "AI suggested — tap to toggle",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7C4DFF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habits.forEachIndexed { idx, (habit, selected) ->
                    HabitSuggestionCard(habit = habit, selected = selected, onClick = { onToggle(idx) })
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No habit suggestions available.\nYou can add habits from the Habit Tracker anytime.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val selectedCount = habits.count { it.second }
        Button(
            onClick = onCreateWithHabits,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors()
        ) {
            Icon(PhosphorIcons.Regular.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (selectedCount > 0) "Create Goal + $selectedCount Habit${if (selectedCount > 1) "s" else ""}"
                else "Create Goal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                "Create goal without habits →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun HabitSuggestionCard(habit: SuggestedHabit, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${habit.emoji} ${habit.title}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                habit.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ) {
            Text(
                habit.frequency.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
