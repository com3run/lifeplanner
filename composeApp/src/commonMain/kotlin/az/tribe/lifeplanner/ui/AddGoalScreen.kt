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
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: GoalViewModel,
    onGoalSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf(GoalCategory.FINANCIAL) }
    var selectedTimeline by remember { mutableStateOf(GoalTimeline.SHORT_TERM) }
    var dueDate by remember {
        mutableStateOf(
            try {
                LocalDate.parse(Clock.System.now().toString().substring(0, 10))
            } catch (e: Exception) {
                LocalDate(2024, 1, 1)
            }
        )
    }
    var dueDateText by remember { mutableStateOf(dueDate.toString()) }

    Scaffold(
        containerColor = MaterialTheme.brutalistColors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.brutalistColors.background,
                ),
                title = { Text("Add Goal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                shape = RectangleShape,
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                shape = RectangleShape,
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
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
                shape = RectangleShape,
                value = dueDateText,
                onValueChange = {
                    dueDateText = it
                    try {
                        dueDate = LocalDate.parse(it)
                    } catch (_: Exception) {}
                },
                label = { Text("Due Date (yyyy-mm-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val goal = Goal(
                        id = Uuid.random().toString(),
                        category = selectedCategory,
                        title = title.text,
                        description = description.text,
                        status = GoalStatus.NOT_STARTED,
                        timeline = selectedTimeline,
                        dueDate = dueDate
                    )
                    viewModel.createGoal(goal)
                    onGoalSaved()
                },
                enabled = title.text.isNotBlank() && dueDateText.matches(Regex("\\d{4}-\\d{2}-\\d{2}")),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.brutalistColors.primaryContainer,
                    contentColor = MaterialTheme.brutalistColors.textPrimary
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                shape = RectangleShape
            ) {
                Text("Save")
            }
        }
    }
}


@Composable
fun <T> MyDropdownMenuBox(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }, shape = RectangleShape) {
            Text(
                selected.toString()
                    .lowercase()
                    .replace('_', ' ')
                    .split(' ')
                    .joinToString(" ") { it.replaceFirstChar(Char::uppercase) },
                color = MaterialTheme.brutalistColors.textPrimary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Text(
                            it.toString()
                                .lowercase()
                                .replace('_', ' ')
                                .split(' ')
                                .joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }
                        )
                    },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}