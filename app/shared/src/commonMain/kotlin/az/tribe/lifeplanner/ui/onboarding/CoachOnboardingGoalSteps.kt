package az.tribe.lifeplanner.ui.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Circle

// ─── Mind questions step ──────────────────────────────────────────────────────

@Composable
internal fun MindQuestionsStep(
    questions: List<String>,
    answers: List<String>,
    currentIndex: Int,
    isProcessing: Boolean,
    onAnswer: (String) -> Unit
) {
    if (questions.isEmpty()) return
    val currentQ = questions.getOrNull(currentIndex) ?: return
    var inputText by remember(currentIndex) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Progress indicator: "Question 1 of 2"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            questions.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (idx < currentIndex) MaterialTheme.colorScheme.primary
                            else if (idx == currentIndex) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Text(
            text = currentQ,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Type your answer…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        if (isProcessing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(
                    "  Analysing…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Button(
                onClick = { onAnswer(inputText.trim()); inputText = "" },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = inputText.isNotBlank()
            ) {
                Text(
                    if (currentIndex < questions.size - 1) "Next →" else "Show me what you see →",
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = { onAnswer(""); inputText = "" },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Skip this question", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

// ─── Mind validation step ─────────────────────────────────────────────────────

@Composable
internal fun MindValidationStep(
    interpretation: GoalInterpretation?,
    qaContext: List<Pair<String, String>>,
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (qaContext.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Your answers",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    qaContext.forEachIndexed { idx, (q, a) ->
                        if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(q, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(a, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Text(
            "I've got a clear picture of what you're after. Ready to commit to this direction?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onConfirm,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                Text("  Mapping your path…", fontWeight = FontWeight.SemiBold)
            } else {
                Text("Yes, this is my direction →", fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = onReject,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(PhosphorIcons.Regular.ArrowCounterClockwise, null, modifier = Modifier.size(16.dp))
            Text("  Let me adjust my answers", fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Goal preview step ────────────────────────────────────────────────────────

@Composable
internal fun GoalPreviewStep(
    goal: az.tribe.lifeplanner.domain.model.Goal?,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    if (goal == null) { onSkip(); return }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text(
                            goal.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (goal.description.isNotBlank()) {
                    Text(
                        goal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (goal.milestones.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Milestones:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        goal.milestones.forEach { milestone ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    milestone.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        PrimaryButton(text = "Add supporting habits →", onClick = onContinue)
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Go to home →", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Habit suggest step ───────────────────────────────────────────────────────

@Composable
internal fun HabitSuggestStep(
    habits: List<Pair<OnboardingHabitItem, Boolean>>,
    onToggle: (Int) -> Unit,
    onContinue: () -> Unit,
    isSaving: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        habits.forEachIndexed { index, (habit, selected) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .clickable { onToggle(index) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (selected) PhosphorIcons.Regular.CheckCircle else PhosphorIcons.Regular.Circle,
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
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val selectedCount = habits.count { it.second }
        Button(
            onClick = onContinue,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(
                if (selectedCount > 0) "Start with $selectedCount habit${if (selectedCount > 1) "s" else ""}!"
                else "Start without habits",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
