package az.tribe.lifeplanner.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Reminder
import az.tribe.lifeplanner.domain.model.ReminderFrequency
import az.tribe.lifeplanner.domain.model.ReminderType
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Alarm
import com.adamglin.phosphoricons.regular.Barbell
import com.adamglin.phosphoricons.regular.Bell
import com.adamglin.phosphoricons.regular.BellSlash
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Columns
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.Flower
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.Sun
import com.mmk.kmpnotifier.notification.NotifierManager
import kotlinx.datetime.LocalTime

// ── Grouped Reminder Section ────────────────────────────────────────

internal data class ReminderSection(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val reminders: List<Reminder>
)

internal fun groupReminders(reminders: List<Reminder>): List<ReminderSection> {
    val goalReminders = reminders.filter {
        it.type == ReminderType.GOAL_DUE || it.type == ReminderType.GOAL_CHECK_IN || it.type == ReminderType.MILESTONE_DUE
    }
    val habitReminders = reminders.filter { it.type == ReminderType.HABIT_REMINDER }
    val wellnessReminders = reminders.filter {
        it.type == ReminderType.DAILY_REFLECTION || it.type == ReminderType.WEEKLY_REVIEW || it.type == ReminderType.MOTIVATION
    }
    val customReminders = reminders.filter { it.type == ReminderType.CUSTOM }

    return listOfNotNull(
        if (goalReminders.isNotEmpty()) ReminderSection(
            "Goals & Milestones", PhosphorIcons.Regular.Flag, Color(0xFF4A6FFF), goalReminders
        ) else null,
        if (habitReminders.isNotEmpty()) ReminderSection(
            "Habits", PhosphorIcons.Regular.Barbell, Color(0xFF28C76F), habitReminders
        ) else null,
        if (wellnessReminders.isNotEmpty()) ReminderSection(
            "Daily Wellness", PhosphorIcons.Regular.Flower, Color(0xFF7A5AF8), wellnessReminders
        ) else null,
        if (customReminders.isNotEmpty()) ReminderSection(
            "Custom", PhosphorIcons.Regular.Bell, Color(0xFF9E9FA3), customReminders
        ) else null
    )
}

// ── Summary Card ────────────────────────────────────────────────────

@Composable
internal fun SummaryCard(reminders: List<Reminder>) {
    val goalCount = reminders.count {
        it.type == ReminderType.GOAL_DUE || it.type == ReminderType.MILESTONE_DUE || it.type == ReminderType.GOAL_CHECK_IN
    }
    val habitCount = reminders.count { it.type == ReminderType.HABIT_REMINDER }
    val wellnessCount = reminders.count {
        it.type == ReminderType.DAILY_REFLECTION || it.type == ReminderType.WEEKLY_REVIEW || it.type == ReminderType.MOTIVATION
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryChip(count = goalCount, label = "Goals", color = Color(0xFF4A6FFF))
            SummaryChip(count = habitCount, label = "Habits", color = Color(0xFF28C76F))
            SummaryChip(count = wellnessCount, label = "Wellness", color = Color(0xFF7A5AF8))
        }
    }
}

@Composable
internal fun SummaryChip(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
    }
}

// ── Section Header ──────────────────────────────────────────────────

@Composable
internal fun ReminderSectionHeader(title: String, icon: ImageVector, color: Color, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.modernColors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(text = "$count", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.modernColors.textSecondary)
    }
}

// ── Global Toggle ───────────────────────────────────────────────────

@Composable
internal fun GlobalReminderToggle(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = if (isEnabled) PhosphorIcons.Regular.Bell else PhosphorIcons.Regular.BellSlash,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isEnabled) "All reminders are active" else "All reminders are paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) NotifierManager.getPermissionUtil().askNotificationPermission()
                    onToggle(enabled)
                }
            )
        }
    }
}

// ── Test Notification ───────────────────────────────────────────────

@Composable
internal fun TestNotificationCard() {
    OutlinedCard(
        onClick = {
            NotifierManager.getLocalNotifier().notify {
                id = 999
                title = "Life Planner"
                body = "Notifications are working!"
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = PhosphorIcons.Regular.Bell, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text("Send Test Notification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Tap to verify notifications work on this device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Smart Timing Card ───────────────────────────────────────────────

@Composable
internal fun SmartTimingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = PhosphorIcons.Regular.Sparkle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            Column {
                Text("Smart Timing Active", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(
                    "Reminders are optimized based on your activity patterns",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ── Empty State ─────────────────────────────────────────────────────

@Composable
internal fun EmptyRemindersCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAddClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = PhosphorIcons.Regular.Alarm, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.modernColors.textSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Reminders Yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.modernColors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Create a goal or habit and we'll set up\nsmart reminders automatically",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.modernColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Color & Icon Helpers ────────────────────────────────────────────

internal fun getReminderTypeColor(type: ReminderType): Color = when (type) {
    ReminderType.GOAL_CHECK_IN -> Color(0xFF4A6FFF)
    ReminderType.HABIT_REMINDER -> Color(0xFF28C76F)
    ReminderType.MILESTONE_DUE -> Color(0xFFFF9F43)
    ReminderType.GOAL_DUE -> Color(0xFFEA5455)
    ReminderType.DAILY_REFLECTION -> Color(0xFF7A5AF8)
    ReminderType.WEEKLY_REVIEW -> Color(0xFF00CFE8)
    ReminderType.MOTIVATION -> Color(0xFF6236FF)
    ReminderType.CUSTOM -> Color(0xFF9E9FA3)
}

internal fun getReminderTypeIcon(type: ReminderType): ImageVector = when (type) {
    ReminderType.GOAL_CHECK_IN -> PhosphorIcons.Regular.Clock
    ReminderType.HABIT_REMINDER -> PhosphorIcons.Regular.Repeat
    ReminderType.MILESTONE_DUE -> PhosphorIcons.Regular.Star
    ReminderType.GOAL_DUE -> PhosphorIcons.Regular.Alarm
    ReminderType.DAILY_REFLECTION -> PhosphorIcons.Regular.Flower
    ReminderType.WEEKLY_REVIEW -> PhosphorIcons.Regular.Columns
    ReminderType.MOTIVATION -> PhosphorIcons.Regular.Sun
    ReminderType.CUSTOM -> PhosphorIcons.Regular.Bell
}

// ── Format Helpers ──────────────────────────────────────────────────

internal fun formatFrequency(reminder: Reminder): String = when (reminder.frequency) {
    ReminderFrequency.ONCE -> "Once"
    ReminderFrequency.DAILY -> "Every day"
    ReminderFrequency.WEEKDAYS -> "Weekdays"
    ReminderFrequency.WEEKENDS -> "Weekends"
    ReminderFrequency.WEEKLY -> {
        val days = reminder.scheduledDays.joinToString(", ") {
            it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
        }
        if (days.isNotBlank()) days else "Weekly"
    }
    ReminderFrequency.MONTHLY -> "Monthly"
    ReminderFrequency.SMART -> "Smart"
}

internal fun formatTime12h(time: LocalTime): String {
    val hour = time.hour
    val minute = time.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "${h12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}
