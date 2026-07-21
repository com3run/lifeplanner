package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.domain.enum.HealthMetricType
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onHabitSaved: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: HabitViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.BODY) }
    var selectedFrequency by remember { mutableStateOf(HabitFrequency.DAILY) }
    var selectedHabitType by remember { mutableStateOf(HabitType.BUILD) }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var healthMetric by remember { mutableStateOf<HealthMetricType?>(null) }
    var healthTargetText by remember { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)
    var selectedTemplateCategory by remember { mutableStateOf<GoalCategory?>(null) }

    val isFormValid = title.isNotBlank()

    fun applyTemplate(template: HabitTemplate) {
        title = template.title
        description = template.description
        selectedCategory = template.category
        selectedFrequency = template.frequency
        showTemplates = false
    }

    // ── Time picker dialog ───────────────────────────────────────────────────
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

    // ── Templates bottom sheet ───────────────────────────────────────────────
    if (showTemplates) {
        val filteredTemplates = remember(selectedTemplateCategory) {
            if (selectedTemplateCategory == null) habitTemplates
            else habitTemplates.filter { it.category == selectedTemplateCategory }
        }
        ModalBottomSheet(
            onDismissRequest = { showTemplates = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    "Habit Templates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Text(
                    "Tap a template to fill the form",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTemplateCategory == null,
                        onClick = { selectedTemplateCategory = null },
                        label = { Text("All") },
                        shape = RoundedCornerShape(50)
                    )
                    GoalCategory.entries.forEach { cat ->
                        if (habitTemplates.any { it.category == cat }) {
                            FilterChip(
                                selected = selectedTemplateCategory == cat,
                                onClick = {
                                    selectedTemplateCategory =
                                        if (selectedTemplateCategory == cat) null else cat
                                },
                                label = { Text("${cat.emoji()} ${cat.name.lowercase().replaceFirstChar { it.uppercase() }}") },
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Template cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredTemplates.forEach { template ->
                        Surface(
                            onClick = { applyTemplate(template) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.width(148.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(template.gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    template.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = template.gradientColors.first().copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        template.frequency.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = template.gradientColors.first(),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Main scaffold ────────────────────────────────────────────────────────
    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary,
                ),
                title = {
                    Text(
                        "New Habit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.modernColors.textPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        viewModel.createHabit(
                            title = title.trim(),
                            description = description.trim(),
                            category = selectedCategory,
                            frequency = selectedFrequency,
                            type = selectedHabitType,
                            reminderTime = reminderTime,
                            healthMetricType = healthMetric,
                            healthTarget = healthTargetText.toDoubleOrNull()
                        )
                        onHabitSaved()
                    }
                },
                text = { Text("Save Habit") },
                icon = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Save"
                    )
                },
                expanded = isFormValid,
                shape = RoundedCornerShape(16.dp),
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isFormValid) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ── Title ────────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit name") },
                    placeholder = { Text("e.g., Morning meditation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Description ──────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("e.g., 10 minutes of mindfulness") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Habit type toggle ────────────────────────────────────────────
            item {
                Text(
                    "Habit type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitType.entries.forEach { type ->
                        val isSelected = selectedHabitType == type
                        val color = if (type == HabitType.BUILD) Color(0xFF4CAF50) else Color(0xFFF44336)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedHabitType = type },
                            label = {
                                Text(
                                    if (type == HabitType.BUILD) "Build" else "Break bad habit",
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.12f),
                                selectedLabelColor = color
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Category chips ───────────────────────────────────────────────
            item {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalCategory.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val chipColor = cat.chipColor()
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    "${cat.emoji()} ${cat.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.14f),
                                selectedLabelColor = chipColor
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // ── Frequency chips ──────────────────────────────────────────────
            item {
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
                        val isSelected = selectedFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFrequency = freq },
                            label = {
                                Text(
                                    freq.displayName,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Time of day ──────────────────────────────────────────────────
            item {
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
                                    val slot = when (h) {
                                        in 5..11 -> "Morning"
                                        in 12..16 -> "Afternoon"
                                        else -> "Evening"
                                    }
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
            }

            // ── Sync with Health ─────────────────────────────────────────────
            item {
                Text(
                    "Sync with Health",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Linked habits check themselves off when your health data hits the target.",
                    style = MaterialTheme.typography.bodySmall,
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
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (healthMetric != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Completes when any ${healthMetric!!.displayName.lowercase()} reading is recorded that day.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Browse templates ─────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { showTemplates = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse habit templates")
                }
            }

            // Bottom spacing for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun GoalCategory.emoji() = when (this) {
    GoalCategory.CAREER -> "💼"
    GoalCategory.MONEY -> "💰"
    GoalCategory.BODY -> "💪"
    GoalCategory.PEOPLE -> "👥"
    GoalCategory.WELLBEING -> "💛"
    GoalCategory.PURPOSE -> "🙏"
    GoalCategory.FAMILY -> "🏡"
}

private fun GoalCategory.chipColor() = when (this) {
    GoalCategory.CAREER -> Color(0xFF2196F3)
    GoalCategory.MONEY -> Color(0xFF11998E)
    GoalCategory.BODY -> Color(0xFFFF8008)
    GoalCategory.PEOPLE -> Color(0xFF9C27B0)
    GoalCategory.WELLBEING -> Color(0xFF009688)
    GoalCategory.PURPOSE -> Color(0xFFE91E63)
    GoalCategory.FAMILY -> Color(0xFFF57C00)
}
