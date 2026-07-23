package az.tribe.lifeplanner.ui.focus

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Clock

@Composable
internal fun FocusCompleteContent(
    focusViewModel: FocusViewModel,
    onDone: () -> Unit,
    onStartAnother: () -> Unit,
    wasCancelled: Boolean = false
) {
    val lastXpEarned by focusViewModel.lastXpEarned.collectAsState()
    val selectedGoal by focusViewModel.selectedGoal.collectAsState()
    val selectedMilestone by focusViewModel.selectedMilestone.collectAsState()
    val elapsedSeconds by focusViewModel.elapsedSeconds.collectAsState()
    val selectedMood by focusViewModel.selectedMood.collectAsState()

    val elapsedDisplay = formatDuration(elapsedSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = LifePlannerDesign.Padding.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    brush = if (wasCancelled)
                        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                    else
                        Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (wasCancelled) PhosphorIcons.Regular.Clock else PhosphorIcons.Regular.Check,
                null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            if (wasCancelled) "Session Ended" else "Focus Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        selectedMilestone?.let { milestone ->
            Text(
                milestone.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "$elapsedDisplay focused",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "How are you feeling?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Mood.entries.sortedBy { it.score }.forEach { mood ->
                MoodChip(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onClick = { focusViewModel.setMood(mood) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (lastXpEarned > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFF6B35).copy(alpha = 0.12f)
            ) {
                Text(
                    "+$lastXpEarned XP",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
        }

        val showMilestonePrompt by focusViewModel.showMilestonePrompt.collectAsState()
        val milestoneMarkedComplete by focusViewModel.milestoneMarkedComplete.collectAsState()
        val canCompleteMilestone by focusViewModel.canCompleteMilestone.collectAsState()

        if (showMilestonePrompt) selectedMilestone?.let { milestone ->
            Spacer(Modifier.height(24.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Did you complete this milestone?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        milestone.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (!canCompleteMilestone) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Complete at least one focus session first",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { focusViewModel.dismissMilestonePrompt() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Need more time")
                        }
                        Button(
                            onClick = { focusViewModel.markMilestoneComplete() },
                            enabled = canCompleteMilestone,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(PhosphorIcons.Regular.Check, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Yes, done!")
                        }
                    }
                }
            }
        }

        if (milestoneMarkedComplete) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF4CAF50).copy(alpha = 0.12f)
            ) {
                Text(
                    "Milestone completed!",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onStartAnother,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
        ) {
            Text("Start Another", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
    }
}
