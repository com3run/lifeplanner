package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.isTimeUnit
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.X

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditHabitBottomSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (Habit) -> Unit
) {
    var title by remember { mutableStateOf(habit.title) }
    var description by remember { mutableStateOf(habit.description) }
    var selectedCategory by remember { mutableStateOf(habit.category) }
    var selectedFrequency by remember { mutableStateOf(habit.frequency) }
    var expandedCategory by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf(habit.reminderTime) }
    var showTimePicker by remember { mutableStateOf(false) }
    var healthMetric by remember { mutableStateOf(habit.healthMetricType) }
    var healthTargetText by remember {
        mutableStateOf(habit.healthTarget?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
        } ?: "")
    }
    // Use the shared rule rather than re-deriving it. The local copy tested `unit == "min"`, so
    // seconds and hours habits opened as COUNT and a 30-sec plank looked like 30 reps.
    var trackMode by remember { mutableStateOf(habit.trackMode) }
    var countTargetText by remember {
        mutableStateOf(if (habit.targetCount > 1) habit.targetCount.toString() else "")
    }
    var countUnitText by remember {
        mutableStateOf(habit.unit?.takeIf { !isTimeUnit(it) } ?: "")
    }
    var completionSource by remember { mutableStateOf(habit.completionSource) }
    val timePickerState = rememberTimePickerState(
        initialHour = habit.reminderTime?.split(":")?.firstOrNull()?.toIntOrNull() ?: 8,
        initialMinute = habit.reminderTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = false
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Time of day") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    showTimePicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        // Scrollable: the form is taller than the sheet on ordinary phone screens, so without this
        // the Save button sits below the fold and the sheet cannot be submitted at all.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Habit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close"
                    )
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        GoalCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency chips
                Text(
                    "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(HabitFrequency.DAILY, HabitFrequency.WEEKDAYS, HabitFrequency.WEEKLY).forEach { freq ->
                        FilterChip(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            label = {
                                Text(
                                    freq.displayName,
                                    fontWeight = if (selectedFrequency == freq) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time of day
                Text(
                    "Time of day",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = { showTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Clock,
                            contentDescription = null,
                            tint = if (reminderTime != null) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (reminderTime != null) {
                                    val parts = reminderTime!!.split(":")
                                    val h = parts[0].toIntOrNull() ?: 8
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    val period = if (h < 12) "AM" else "PM"
                                    val dh = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
                                    val slot = when (h) { in 5..11 -> "Morning"; in 12..16 -> "Afternoon"; else -> "Evening" }
                                    "$slot · $dh:${m.toString().padStart(2, '0')} $period"
                                } else "Tap to set, used for habit grouping",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (reminderTime != null) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (reminderTime != null) {
                            IconButton(
                                onClick = { reminderTime = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.X,
                                    contentDescription = "Clear time",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // How do you track it
                Text(
                    "How do you track it?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitTrackMode.entries.forEach { mode ->
                        FilterChip(
                            selected = trackMode == mode,
                            onClick = { trackMode = mode },
                            label = {
                                Text(
                                    mode.label,
                                    fontWeight = if (trackMode == mode) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (trackMode == HabitTrackMode.COUNT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = countTargetText,
                            onValueChange = { countTargetText = it.filter { c -> c.isDigit() } },
                            label = { Text("Times a day") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = countUnitText,
                            onValueChange = { countUnitText = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                } else if (trackMode == HabitTrackMode.DURATION) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = countTargetText,
                        onValueChange = { countTargetText = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes a day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CompletionSourcePicker(
                    selected = completionSource,
                    trackMode = trackMode,
                    targetCount = countTargetText.toIntOrNull() ?: 1,
                    onSelect = { completionSource = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sync with Health
                Text(
                    "Sync with Health",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = healthMetric == null,
                        onClick = { healthMetric = null; healthTargetText = "" },
                        label = { Text("None") }
                    )
                    listOf(HealthMetricType.STEPS, HealthMetricType.SLEEP).forEach { metric ->
                        FilterChip(
                            selected = healthMetric == metric,
                            onClick = {
                                healthMetric = metric
                                healthTargetText = when (metric) {
                                    HealthMetricType.STEPS -> "10000"
                                    HealthMetricType.SLEEP -> "8"
                                    else -> ""
                                }
                            },
                            label = { Text(metric.displayName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(HealthMetricType.HEART_RATE, HealthMetricType.WEIGHT).forEach { metric ->
                        FilterChip(
                            selected = healthMetric == metric,
                            onClick = { healthMetric = metric; healthTargetText = "" },
                            label = { Text(metric.displayName) }
                        )
                    }
                }
                if (healthMetric == HealthMetricType.STEPS || healthMetric == HealthMetricType.SLEEP) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = healthTargetText,
                        onValueChange = { healthTargetText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = {
                            Text(if (healthMetric == HealthMetricType.STEPS) "Daily step target" else "Hours of sleep")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val target = countTargetText.toIntOrNull()?.coerceAtLeast(1)
                            onConfirm(
                                habit.copy(
                                    title = title,
                                    description = description,
                                    category = selectedCategory,
                                    frequency = selectedFrequency,
                                    targetCount = when (trackMode) {
                                        HabitTrackMode.SINGLE -> 1
                                        else -> target ?: 1
                                    },
                                    unit = when (trackMode) {
                                        HabitTrackMode.SINGLE -> null
                                        HabitTrackMode.COUNT -> countUnitText.trim().ifBlank { "times" }
                                        // Keep the unit it already had. Hardcoding "min" turned a
                                        // 30-second habit into a 30-minute one on any edit.
                                        HabitTrackMode.DURATION ->
                                            habit.unit?.takeIf { isTimeUnit(it) } ?: "min"
                                    },
                                    reminderTime = reminderTime,
                                    healthMetricType = healthMetric,
                                    healthTarget = healthTargetText.toDoubleOrNull(),
                                    completionSource = completionSource
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
