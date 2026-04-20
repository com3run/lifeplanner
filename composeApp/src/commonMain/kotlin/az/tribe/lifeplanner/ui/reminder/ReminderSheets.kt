package az.tribe.lifeplanner.ui.reminder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.DayOfWeek
import az.tribe.lifeplanner.domain.model.Reminder
import az.tribe.lifeplanner.domain.model.ReminderFrequency
import az.tribe.lifeplanner.domain.model.ReminderSettings
import az.tribe.lifeplanner.domain.model.ReminderType
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Trash
import kotlin.time.Clock as KtClock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddReminderSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String, ReminderType, ReminderFrequency, LocalTime, List<DayOfWeek>, Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReminderType.CUSTOM) }
    var selectedFrequency by remember { mutableStateOf(ReminderFrequency.DAILY) }
    var selectedDays by remember { mutableStateOf<List<DayOfWeek>>(emptyList()) }
    var smartTiming by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)

    if (showTimePicker) {
        TimePickerDialog(state = timePickerState, onDismiss = { showTimePicker = false }, onConfirm = { showTimePicker = false })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val types = listOf(
                    ReminderType.CUSTOM to "Custom",
                    ReminderType.GOAL_CHECK_IN to "Goal",
                    ReminderType.DAILY_REFLECTION to "Reflection",
                    ReminderType.MOTIVATION to "Motivation"
                )
                types.forEach { (type, label) ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it }, label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = message, onValueChange = { message = it }, label = { Text("Message (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            val timeText = formatTime12h(LocalTime(timePickerState.hour, timePickerState.minute))
            OutlinedCard(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(PhosphorIcons.Regular.Clock, contentDescription = "Pick time", tint = MaterialTheme.colorScheme.primary)
                }
            }

            ReminderFrequencySelector(selectedFrequency = selectedFrequency, onFrequencyChanged = { selectedFrequency = it })

            AnimatedVisibility(visible = selectedFrequency == ReminderFrequency.WEEKLY) {
                ReminderDaySelector(selectedDays = selectedDays, onDaysChanged = { selectedDays = it })
            }

            ReminderSmartTimingRow(smartTiming = smartTiming, onSmartTimingChanged = { smartTiming = it })

            if (title.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(PhosphorIcons.Regular.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Preview: \"$title\" at $timeText", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                formatFrequency(
                                    Reminder(
                                        id = "", title = "", message = "", type = selectedType,
                                        frequency = selectedFrequency, scheduledTime = LocalTime(0, 0),
                                        scheduledDays = selectedDays,
                                        createdAt = KtClock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    )
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedCard(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("Cancel", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(title, message, selectedType, selectedFrequency, LocalTime(timePickerState.hour, timePickerState.minute), selectedDays, smartTiming)
                        }
                    },
                    enabled = title.isNotBlank(), modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Create Reminder", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditReminderSheet(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var title by remember { mutableStateOf(reminder.title) }
    var message by remember { mutableStateOf(reminder.message) }
    var selectedFrequency by remember { mutableStateOf(reminder.frequency) }
    var selectedDays by remember { mutableStateOf(reminder.scheduledDays) }
    var smartTiming by remember { mutableStateOf(reminder.isSmartTiming) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = reminder.scheduledTime.hour, initialMinute = reminder.scheduledTime.minute, is24Hour = false)
    val isAuto = reminder.id.startsWith("auto-")

    if (showTimePicker) {
        TimePickerDialog(state = timePickerState, onDismiss = { showTimePicker = false }, onConfirm = { showTimePicker = false })
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete \"${reminder.title}\"?") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Edit Reminder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (isAuto) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.tertiaryContainer).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("AUTO-MANAGED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            if (isAuto) {
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                ) {
                    Text(
                        "This reminder was auto-created from your goals or habits. You can customize the time and frequency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it }, label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = message, onValueChange = { message = it }, label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            val timeText = formatTime12h(LocalTime(timePickerState.hour, timePickerState.minute))
            OutlinedCard(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(PhosphorIcons.Regular.Clock, contentDescription = "Pick time", tint = MaterialTheme.colorScheme.primary)
                }
            }

            ReminderFrequencySelector(selectedFrequency = selectedFrequency, onFrequencyChanged = { selectedFrequency = it })

            AnimatedVisibility(visible = selectedFrequency == ReminderFrequency.WEEKLY) {
                ReminderDaySelector(selectedDays = selectedDays, onDaysChanged = { selectedDays = it })
            }

            ReminderSmartTimingRow(smartTiming = smartTiming, onSmartTimingChanged = { smartTiming = it })

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(PhosphorIcons.Regular.Trash, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
                Spacer(Modifier.weight(1f))
                OutlinedCard(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { Text("Cancel", style = MaterialTheme.typography.titleSmall) }
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(reminder.copy(title = title, message = message, frequency = selectedFrequency, scheduledTime = LocalTime(timePickerState.hour, timePickerState.minute), scheduledDays = selectedDays, isSmartTiming = smartTiming))
                        }
                    },
                    enabled = title.isNotBlank(), shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) { Text("Save", style = MaterialTheme.typography.titleSmall) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(state: TimePickerState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", style = MaterialTheme.typography.titleMedium) },
        text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderSettingsSheet(settings: ReminderSettings, onDismiss: () -> Unit, onSave: (ReminderSettings) -> Unit) {
    var localSettings by remember { mutableStateOf(settings) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Reminder Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SettingsRow(icon = PhosphorIcons.Regular.Sparkle, title = "Smart Timing", subtitle = "Optimize reminder times based on your activity") {
                Switch(checked = localSettings.smartTimingEnabled, onCheckedChange = { localSettings = localSettings.copy(smartTimingEnabled = it) })
            }
            HorizontalDivider()
            SettingsRow(icon = PhosphorIcons.Regular.Moon, title = "Quiet Hours", subtitle = "${localSettings.quietHoursStart} - ${localSettings.quietHoursEnd}") {}
            SettingsRow(icon = PhosphorIcons.Regular.Repeat, title = "Max Reminders/Day", subtitle = "${localSettings.maxRemindersPerDay} reminders") {}
            SettingsRow(
                icon = PhosphorIcons.Regular.CalendarBlank,
                title = "Weekly Review",
                subtitle = "${localSettings.weeklyReviewDay.name.lowercase().replaceFirstChar { it.uppercase() }} at ${localSettings.weeklyReviewTime}"
            ) {}

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onSave(localSettings) }, modifier = Modifier.fillMaxWidth()) { Text("Save Settings") }
        }
    }
}

@Composable
internal fun SettingsRow(icon: ImageVector, title: String, subtitle: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.textSecondary)
            }
        }
        trailing()
    }
}

// ── Shared Form Components ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderFrequencySelector(selectedFrequency: ReminderFrequency, onFrequencyChanged: (ReminderFrequency) -> Unit) {
    Text("Repeat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val frequencies = listOf(ReminderFrequency.ONCE to "Once", ReminderFrequency.DAILY to "Daily", ReminderFrequency.WEEKDAYS to "Weekdays", ReminderFrequency.WEEKLY to "Weekly")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        frequencies.forEachIndexed { index, (freq, label) ->
            SegmentedButton(
                selected = selectedFrequency == freq,
                onClick = { onFrequencyChanged(freq) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = frequencies.size)
            ) { Text(label, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun ReminderDaySelector(selectedDays: List<DayOfWeek>, onDaysChanged: (List<DayOfWeek>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in selectedDays,
                    onClick = { onDaysChanged(if (day in selectedDays) selectedDays - day else selectedDays + day) },
                    label = { Text(day.name.take(2), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ReminderSmartTimingRow(smartTiming: Boolean, onSmartTimingChanged: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Smart Timing", style = MaterialTheme.typography.bodyMedium)
            Text("Auto-optimize based on activity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = smartTiming, onCheckedChange = onSmartTimingChanged)
    }
}
