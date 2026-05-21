package az.tribe.lifeplanner.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X

@Composable
internal fun ContextAndGenerateStep(
    goals: List<Goal>,
    habits: List<Habit>,
    selectedGoalId: String?,
    selectedHabitId: String?,
    userNote: String,
    isGenerating: Boolean,
    isOffline: Boolean = false,
    onGoalSelected: (String?) -> Unit,
    onHabitSelected: (String?) -> Unit,
    onNoteChanged: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onSkipAiClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showGoalDropdown by remember { mutableStateOf(false) }
    var showHabitDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Link a goal or habit and add any extra context for a richer entry.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Goal/Habit pickers
        if (goals.isNotEmpty() || habits.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (goals.isNotEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { showGoalDropdown = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedGoalId != null)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selectedGoalId != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedGoalId?.let { id ->
                                        goals.find { it.id == id }?.title ?: "Goal"
                                    } ?: "Goal",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedGoalId != null) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.X,
                                        contentDescription = "Clear",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onGoalSelected(null) },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showGoalDropdown,
                            onDismissRequest = { showGoalDropdown = false }
                        ) {
                            goals.take(10).forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onGoalSelected(goal.id)
                                        showGoalDropdown = false
                                    },
                                    leadingIcon = {
                                        Icon(PhosphorIcons.Regular.Flag, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }

                if (habits.isNotEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { showHabitDropdown = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedHabitId != null)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (selectedHabitId != null)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedHabitId?.let { id ->
                                        habits.find { it.id == id }?.title ?: "Habit"
                                    } ?: "Habit",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedHabitId != null) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.X,
                                        contentDescription = "Clear",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onHabitSelected(null) },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showHabitDropdown,
                            onDismissRequest = { showHabitDropdown = false }
                        ) {
                            habits.take(10).forEach { habit ->
                                DropdownMenuItem(
                                    text = { Text(habit.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onHabitSelected(habit.id)
                                        showHabitDropdown = false
                                    },
                                    leadingIcon = {
                                        Icon(PhosphorIcons.Regular.Repeat, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Optional note
        OutlinedTextField(
            value = userNote,
            onValueChange = onNoteChanged,
            label = { Text("Quick note (optional)") },
            placeholder = { Text("Any extra context for AI...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Offline hint
        if (isOffline) {
            Text(
                text = "AI generation requires internet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Generate button
        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isGenerating && !isOffline
        ) {
            Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Generate with AI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Skip AI link
        TextButton(
            onClick = onSkipAiClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Skip AI, write manually",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
