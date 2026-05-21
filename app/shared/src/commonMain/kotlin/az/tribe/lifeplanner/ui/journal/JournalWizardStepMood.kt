package az.tribe.lifeplanner.ui.journal

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.Mood

@Composable
internal fun MoodSelectionStep(
    selectedMood: Mood?,
    onMoodSelected: (Mood) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tap how you feel right now",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Mood.entries.sortedBy { it.score }.forEach { mood ->
                val isSelected = mood == selectedMood
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "moodScale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onMoodSelected(mood) }
                        .scale(scale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mood.emoji,
                            fontSize = 36.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mood.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
