package az.tribe.lifeplanner.ui.retrospective

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.GoalChangeWithTitle
import az.tribe.lifeplanner.ui.components.GlassCard

@Composable
internal fun GoalChangesSection(changes: List<GoalChangeWithTitle>) {
    // Sort by time and group changes by the same goal
    val sortedChanges = changes.sortedBy { it.changedAt }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sortedChanges.forEachIndexed { index, change ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Timeline with time label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(52.dp).padding(end = 8.dp)
                    ) {
                        Text(
                            formatTime(change.changedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (change.field.lowercase()) {
                                        "status" -> MaterialTheme.colorScheme.primary
                                        "progress" -> Color(0xFF4CAF50)
                                        else -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                        )
                        if (index < sortedChanges.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            change.goalTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            buildChangeDescription(change),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < sortedChanges.size - 1) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

internal fun buildChangeDescription(change: GoalChangeWithTitle): String {
    return when (change.field.lowercase()) {
        "progress" -> {
            val old = change.oldValue?.toIntOrNull() ?: 0
            val new = change.newValue?.toIntOrNull() ?: 0
            if (new > old) "Progress increased from $old% to $new%"
            else if (new < old) "Progress adjusted from $old% to $new%"
            else "Progress is at $new%"
        }
        "status" -> {
            val old = formatStatus(change.oldValue)
            val new = formatStatus(change.newValue)
            "Moved from $old to $new"
        }
        "notes" -> "Notes were updated"
        "title" -> "Renamed to \"${change.newValue ?: "?"}\""
        "description" -> "Description was updated"
        "category" -> "Category changed to ${formatCategory(change.newValue)}"
        "timeline" -> "Timeline changed to ${formatTimeline(change.newValue)}"
        "duedate", "due_date" -> "Due date updated to ${change.newValue ?: "?"}"
        else -> "${formatFieldName(change.field)} was updated"
    }
}

internal fun formatStatus(status: String?): String {
    return when (status?.uppercase()) {
        "NOT_STARTED" -> "Not Started"
        "IN_PROGRESS" -> "In Progress"
        "COMPLETED" -> "Completed"
        "ON_HOLD" -> "On Hold"
        "CANCELLED" -> "Cancelled"
        else -> status?.replace("_", " ")?.lowercase()
            ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
    }
}

internal fun formatCategory(category: String?): String {
    return category?.replace("_", " ")?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
}

internal fun formatTimeline(timeline: String?): String {
    return when (timeline?.uppercase()) {
        "SHORT_TERM" -> "Short Term"
        "MID_TERM" -> "Mid Term"
        "LONG_TERM" -> "Long Term"
        else -> timeline?.replace("_", " ")?.lowercase()
            ?.replaceFirstChar { it.uppercase() } ?: "Unknown"
    }
}

internal fun formatFieldName(field: String): String {
    return field.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }
}

internal fun formatTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    val hour = dateTime.hour
    val minute = dateTime.minute.toString().padStart(2, '0')
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return "$displayHour:$minute $amPm"
}
