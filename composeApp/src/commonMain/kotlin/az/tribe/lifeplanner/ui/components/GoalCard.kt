package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.ui.theme.brutalistColors

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
            Text("Status: ${goal.status.name}", style = MaterialTheme.typography.bodySmall)
        }
    }
}