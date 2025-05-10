package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.GoalTimeline
import az.tribe.lifeplanner.ui.theme.brutalistColors

@Composable
fun TimelineTabs(
    selected: GoalTimeline,
    onSelect: (GoalTimeline) -> Unit
) {
    val timelines = GoalTimeline.entries.toTypedArray()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        timelines.forEach { timeline ->
            val isSelected = selected == timeline
            val containerColor = when (timeline) {
                GoalTimeline.SHORT_TERM -> Color(0xFFFF3B3B)   // Bright red
                GoalTimeline.MID_TERM -> Color(0xFFFFEB3B)     // Yellow
                GoalTimeline.LONG_TERM -> Color(0xFF42A5F5)    // Blue
            }
            OutlinedButton(
                onClick = { onSelect(timeline) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) containerColor else MaterialTheme.brutalistColors.buttonText,
                    contentColor =  MaterialTheme.brutalistColors.buttonPrimary
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.brutalistColors.buttonPrimary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = timeline.name.replace("_", " ")
                        .lowercase()
                        .replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}