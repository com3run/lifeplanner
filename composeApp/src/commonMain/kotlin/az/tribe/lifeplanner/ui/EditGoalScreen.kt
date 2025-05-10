package az.tribe.lifeplanner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.*
import az.tribe.lifeplanner.ui.theme.brutalistColors
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
    var title by remember { mutableStateOf(TextFieldValue(originalGoal?.title ?: "")) }
    var description by remember { mutableStateOf(TextFieldValue(originalGoal?.description ?: "")) }
    var selectedCategory by remember { mutableStateOf(originalGoal?.category ?: GoalCategory.FINANCIAL) }
    var selectedTimeline by remember { mutableStateOf(originalGoal?.timeline ?: GoalTimeline.SHORT_TERM) }
    var dueDate by remember { mutableStateOf(originalGoal?.dueDate ?: LocalDate(2025, 1, 1)) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.brutalistColors.background,
                ),
                title = { Text("Edit Goal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Category:")
                MyDropdownMenuBox(
                    options = GoalCategory.entries,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Timeline:")
                MyDropdownMenuBox(
                    options = GoalTimeline.entries,
                    selected = selectedTimeline,
                    onSelected = { selectedTimeline = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        originalGoal?.copy(
                            title = title.text,
                            description = description.text,
                            category = selectedCategory,
                            timeline = selectedTimeline,
                            dueDate = dueDate
                        )?.let {
                            viewModel.updateGoal(it)
                            onGoalUpdated()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.brutalistColors.primaryContainer,
                        contentColor = MaterialTheme.brutalistColors.textPrimary
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    shape = RectangleShape
                ) {
                    Text("Update")
                }
            }
        }
    )
}
