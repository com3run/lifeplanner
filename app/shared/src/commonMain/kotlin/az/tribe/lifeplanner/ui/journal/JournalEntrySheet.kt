package az.tribe.lifeplanner.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.JournalPrompts
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_clear
import leanlifeplanner.app.shared.generated.resources.cd_clear_prompt
import leanlifeplanner.app.shared.generated.resources.cd_close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewJournalEntryBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Mood, List<String>, String?, String?, String?) -> Unit,
    goals: List<Goal> = emptyList(),
    habits: List<Habit> = emptyList(),
    preselectedGoalId: String? = null,
    preselectedHabitId: String? = null,
    viewModel: JournalViewModel,
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Mood.NEUTRAL) }
    var tagsText by remember { mutableStateOf("") }
    var selectedGoalId by remember { mutableStateOf(preselectedGoalId) }
    var selectedHabitId by remember { mutableStateOf(preselectedHabitId) }
    var showGoalDropdown by remember { mutableStateOf(false) }
    var showHabitDropdown by remember { mutableStateOf(false) }
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()
    var selectedPrompt by remember { mutableStateOf<String?>(null) }
    var showPromptLibrary by remember { mutableStateOf(false) }
    val haptic = rememberHapticManager()

    LaunchedEffect(aiResult) {
        val result = aiResult ?: return@LaunchedEffect
        if (title.isBlank()) title = result.title
        content = result.content
        if (result.tags.isNotEmpty()) tagsText = result.tags.joinToString(", ")
        viewModel.clearAiResult()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Journal Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = PhosphorIcons.Regular.X, contentDescription = stringResource(Res.string.cd_close))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("How are you feeling?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MoodPicker(selectedMood = selectedMood, onMoodSelected = { selectedMood = it })
                }

                if (goals.isNotEmpty() || habits.isNotEmpty()) {
                    item {
                        Text("Link to (optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (goals.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        onClick = { showGoalDropdown = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedGoalId != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(PhosphorIcons.Regular.Flag, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (selectedGoalId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedGoalId?.let { id -> goals.find { it.id == id }?.title ?: "Goal" } ?: "Goal", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            if (selectedGoalId != null) Icon(PhosphorIcons.Regular.X, contentDescription = stringResource(Res.string.cd_clear), modifier = Modifier.size(16.dp).clickable { selectedGoalId = null }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    DropdownMenu(expanded = showGoalDropdown, onDismissRequest = { showGoalDropdown = false }) {
                                        goals.take(10).forEach { goal ->
                                            DropdownMenuItem(
                                                text = { Text(goal.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                onClick = { selectedGoalId = goal.id; showGoalDropdown = false },
                                                leadingIcon = { Icon(PhosphorIcons.Regular.Flag, contentDescription = null) }
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
                                        color = if (selectedHabitId != null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(PhosphorIcons.Regular.Repeat, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (selectedHabitId != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(selectedHabitId?.let { id -> habits.find { it.id == id }?.title ?: "Habit" } ?: "Habit", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            if (selectedHabitId != null) Icon(PhosphorIcons.Regular.X, contentDescription = stringResource(Res.string.cd_clear), modifier = Modifier.size(16.dp).clickable { selectedHabitId = null }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    DropdownMenu(expanded = showHabitDropdown, onDismissRequest = { showHabitDropdown = false }) {
                                        habits.take(10).forEach { habit ->
                                            DropdownMenuItem(
                                                text = { Text(habit.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                onClick = { selectedHabitId = habit.id; showHabitDropdown = false },
                                                leadingIcon = { Icon(PhosphorIcons.Regular.Repeat, contentDescription = null) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, placeholder = { Text("What's on your mind?") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Prompt (optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                        Surface(
                            onClick = { showPromptLibrary = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedPrompt != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, tint = if (selectedPrompt != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Text(
                                    text = selectedPrompt ?: "Browse prompt library...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedPrompt != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontStyle = if (selectedPrompt != null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (selectedPrompt != null) Icon(PhosphorIcons.Regular.X, contentDescription = stringResource(Res.string.cd_clear_prompt), modifier = Modifier.size(16.dp).clickable { selectedPrompt = null }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        val canUseAi = title.isNotBlank() || selectedPrompt != null
                        OutlinedButton(
                            onClick = {
                                val linkedGoal = selectedGoalId?.let { id -> goals.find { it.id == id } }
                                val linkedHabit = selectedHabitId?.let { id -> habits.find { it.id == id } }
                                viewModel.generateJournalEntry(mood = selectedMood, prompt = selectedPrompt ?: "", userNote = title, linkedGoal = linkedGoal, linkedHabit = linkedHabit)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canUseAi && !isGeneratingAi,
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = canUseAi)
                        ) {
                            if (isGeneratingAi) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Write with AI")
                            }
                        }

                        aiError?.let { errorMsg ->
                            Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                item {
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Your thoughts") }, placeholder = { Text("Write your reflection...") }, minLines = 5, maxLines = 8, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                item {
                    OutlinedTextField(value = tagsText, onValueChange = { tagsText = it }, label = { Text("Tags (AI will suggest)") }, placeholder = { Text("gratitude, goals, reflection") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                haptic.success()
                                val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                onConfirm(title, content, selectedMood, tags, selectedGoalId, selectedHabitId, selectedPrompt)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(PhosphorIcons.Regular.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showPromptLibrary) {
        PromptLibrarySheet(onDismiss = { showPromptLibrary = false }, onPromptSelected = { prompt -> selectedPrompt = prompt; showPromptLibrary = false })
    }
}

@Composable
fun MoodPicker(selectedMood: Mood, onMoodSelected: (Mood) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Mood.entries.sortedBy { it.score }.forEach { mood ->
            MoodButton(mood = mood, isSelected = mood == selectedMood, onClick = { onMoodSelected(mood) })
        }
    }
}

@Composable
private fun MoodButton(mood: Mood, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = mood.emoji, fontSize = 28.sp)
        }
        if (isSelected) {
            Text(text = mood.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptLibrarySheet(onDismiss: () -> Unit, onPromptSelected: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Prompt Library", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = PhosphorIcons.Regular.X, contentDescription = stringResource(Res.string.cd_close))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { PromptCategory(title = "Daily Reflection", emoji = "🌅", prompts = JournalPrompts.dailyReflection, onPromptClick = onPromptSelected) }
                item { PromptCategory(title = "Goal Reflection", emoji = "🎯", prompts = JournalPrompts.goalReflection, onPromptClick = onPromptSelected) }
                item { PromptCategory(title = "Mood Exploration", emoji = "💭", prompts = JournalPrompts.moodExploration, onPromptClick = onPromptSelected) }
                item { PromptCategory(title = "Weekly Review", emoji = "📊", prompts = JournalPrompts.weeklyReview, onPromptClick = onPromptSelected) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun PromptCategory(title: String, emoji: String, prompts: List<String>, onPromptClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(emoji, fontSize = 20.sp)
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        prompts.forEach { prompt ->
            Surface(onClick = { onPromptClick(prompt) }, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = PhosphorIcons.Regular.Sparkle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    Text(text = prompt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
