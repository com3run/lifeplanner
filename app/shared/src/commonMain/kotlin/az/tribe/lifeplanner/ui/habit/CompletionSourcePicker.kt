package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.HabitCompletionSource
import az.tribe.lifeplanner.domain.service.HabitTrackMode

/**
 * Lets the user say which in-app session completes this habit, so doing the thing inside
 * LifePlanner counts on its own. Shared by the add screen and the edit sheet.
 *
 * The hint under the chips is written for [trackMode] because the crediting rule differs: a
 * minutes habit is filled by the minutes of the session, anything else gains one per session.
 */
@Composable
internal fun CompletionSourcePicker(
    selected: HabitCompletionSource,
    trackMode: HabitTrackMode,
    targetCount: Int,
    onSelect: (HabitCompletionSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "What completes it?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitCompletionSource.entries.forEach { source ->
                val isSelected = selected == source
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(source) },
                    label = {
                        Text(
                            source.displayName,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        val hint = when {
            selected == HabitCompletionSource.MANUAL -> null
            trackMode == HabitTrackMode.DURATION ->
                "Minutes from each ${selected.displayName.lowercase()} count toward your $targetCount."
            trackMode == HabitTrackMode.COUNT ->
                "Each ${selected.displayName.lowercase()} counts as one of $targetCount."
            else -> "One ${selected.displayName.lowercase()} completes it for the day."
        }
        if (hint != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
