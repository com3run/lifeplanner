package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalStatus
import az.tribe.lifeplanner.ui.theme.brutalistColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 1f).takeIf { false } ?: Color.Black,
                shape = RoundedCornerShape(0.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.brutalistColors.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(goal.title, style = MaterialTheme.typography.titleMedium)
            Text("Due: ${goal.dueDate}", style = MaterialTheme.typography.bodyMedium)
            val statusLabel = when (goal.status) {
                GoalStatus.NOT_STARTED -> "🔘 Not Started"
                GoalStatus.IN_PROGRESS -> "🚧 In Progress"
                GoalStatus.COMPLETED -> "🏁 Completed"
            }

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val daysDiff = today.daysUntil(goal.dueDate)
            val progressMessage = when (goal.status) {
                GoalStatus.NOT_STARTED, GoalStatus.IN_PROGRESS -> {
                    if (daysDiff > 0) "🗓 $daysDiff days left"
                    else "⚠️ Overdue by ${-daysDiff} days"
                }
                GoalStatus.COMPLETED -> {
                    "✅ Completed ${-daysDiff} days early"
                }
            }

            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            Text(statusLabel, style = MaterialTheme.typography.bodySmall)

            Text(progressMessage, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}