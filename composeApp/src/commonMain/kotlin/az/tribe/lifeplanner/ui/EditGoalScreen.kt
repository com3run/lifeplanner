package az.tribe.lifeplanner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.GoalCategory
import az.tribe.lifeplanner.domain.GoalStatus
import az.tribe.lifeplanner.domain.GoalTimeline
import az.tribe.lifeplanner.ui.components.formatAsReadableDate
import az.tribe.lifeplanner.ui.theme.brutalistColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Composable screen for editing an existing goal.
 * @param goalId The ID of the goal to edit.
 * @param viewModel The GoalViewModel instance.
 * @param onGoalUpdated Callback to invoke after updating the goal.
 * @param onBackClick Callback to invoke when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(
    goalId: String,
    viewModel: GoalViewModel,
    onGoalUpdated: () -> Unit,
    onBackClick: () -> Unit
) {
    val originalGoal = remember { viewModel.getGoalById(goalId) }
    LaunchedEffect(goalId) {
        viewModel.loadGoalHistory(goalId)
    }
    var title by remember { mutableStateOf(TextFieldValue(originalGoal?.title ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(originalGoal?.description ?: "")) }
    var selectedCategory by remember { mutableStateOf(originalGoal?.category ?: GoalCategory.FINANCIAL) }
    var selectedTimeline by remember { mutableStateOf(originalGoal?.timeline ?: GoalTimeline.SHORT_TERM) }
    var dueDate by remember { mutableStateOf(originalGoal?.dueDate ?: LocalDate(2025, 1, 1)) }
    var progress by remember { mutableStateOf(originalGoal?.progress ?: 0) }
    var selectedStatus by remember { mutableStateOf(originalGoal?.status ?: GoalStatus.NOT_STARTED) }
    var isEditing by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.brutalistColors.background,
                ),
                title = { Text("Goal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (isEditing) {
                            originalGoal?.let { original ->
                                val updatedGoal = original.copy(
                                    title = title.text,
                                    description = description.text,
                                    category = selectedCategory,
                                    timeline = selectedTimeline,
                                    status = selectedStatus,
                                    dueDate = dueDate
                                )
                                viewModel.updateGoal(updatedGoal)

                                if (original.progress != progress) {
                                    viewModel.updateGoalProgress(original.id, progress.toInt())
                                }

                                onGoalUpdated()
                            }
                        }
                        isEditing = !isEditing
                    }) {
                        Text(if (isEditing) "Save" else "Edit")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    )
                } else {
                    Text("🎯 ${title.text}", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    )
                } else {
                    Surface(
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = description.text,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    Text("Category:")
                    MyDropdownMenuBox(
                        options = GoalCategory.entries,
                        selected = selectedCategory,
                        onSelected = { selectedCategory = it }
                    )
                } else {
                    Text("📂 ${selectedCategory.name}")
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    Text("Timeline:")
                    MyDropdownMenuBox(
                        options = GoalTimeline.entries,
                        selected = selectedTimeline,
                        onSelected = { selectedTimeline = it }
                    )
                } else {
                    Text("📅 ${selectedTimeline.name.lowercase()
                        .replace('_', ' ')
                        .split(' ')
                        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }}")
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    Text("Status:")
                    MyDropdownMenuBox(
                        options = GoalStatus.entries,
                        selected = selectedStatus,
                        onSelected = { selectedStatus = it }
                    )
                } else {
                    Text(
                        text = when (selectedStatus) {
                            GoalStatus.NOT_STARTED -> "🔘 Not Started"
                            GoalStatus.IN_PROGRESS -> "🚧 In Progress"
                            GoalStatus.COMPLETED -> "🏁 Completed"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val history = viewModel.goalHistory.collectAsState().value
                    if (history.isNotEmpty()) {
                        Text("📜 Change History", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        history.forEach { log ->
                            val formattedTime = try {
                                Instant.parse(log.changedAt).formatAsReadableDate()
                            } catch (e: Exception) {
                                log.changedAt
                            }
                            Text(
                                "🔄 ${log.field.replace('_', ' ').replaceFirstChar(Char::uppercase)}: '${log.oldValue}' → '${log.newValue}' ($formattedTime)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = dueDate.toString(),
                        onValueChange = {
                            // Accept only valid date strings
                            try {
                                dueDate = LocalDate.parse(it)
                            } catch (_: Exception) { /* ignore invalid input */ }
                        },
                        label = { Text("Due Date (yyyy-mm-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    )
                } else {
                    Text("Due Date: $dueDate")
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    Text("Progress: $progress%")
                    Slider(
                        value = progress.toFloat(),
                        onValueChange = { progress = it.toLong() },
                        valueRange = 0f..100f
                    )
                } else {
                    Text("🔥 Progress: $progress%", style = MaterialTheme.typography.labelLarge)
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(vertical = 8.dp),
                    )
                    if (progress in 1..49) {
                        Text("💡 Keep going!")
                    } else if (progress in 50..89) {
                        Text("⚡ You're getting close!")
                    } else if (progress >= 90) {
                        Text("🏆 Almost there!")
                    }
                }
            }
        }
    )
}
