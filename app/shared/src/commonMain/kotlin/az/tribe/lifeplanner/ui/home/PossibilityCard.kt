package az.tribe.lifeplanner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ActionOption
import az.tribe.lifeplanner.domain.model.ActionOptionType
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * Pillar 2, "Right now you could…" card. Surfaces the engine's 3-5 ranked
 * [ActionOption]s with their fit reasons at the top of Home. Hidden when empty.
 */
@Composable
fun RightNowCard(
    options: List<ActionOption>,
    onOptionClick: (ActionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.modernColors.cardBackground
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Right now you could…",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.modernColors.textPrimary
            )
            options.forEach { option ->
                OptionRow(option = option, onClick = { onOptionClick(option) })
            }
        }
    }
}

@Composable
private fun OptionRow(option: ActionOption, onClick: () -> Unit) {
    val accent = typeColor(option.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.modernColors.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.15f)) {
            Text(
                text = typeLabel(option.type),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.modernColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = option.fitReason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.modernColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun typeColor(type: ActionOptionType): Color = when (type) {
    ActionOptionType.HABIT -> MaterialTheme.modernColors.success
    ActionOptionType.MILESTONE -> MaterialTheme.modernColors.primary
    ActionOptionType.FOCUS -> MaterialTheme.modernColors.accent
    ActionOptionType.GOAL -> MaterialTheme.modernColors.secondary
}

private fun typeLabel(type: ActionOptionType): String = when (type) {
    ActionOptionType.HABIT -> "HABIT"
    ActionOptionType.MILESTONE -> "MILESTONE"
    ActionOptionType.FOCUS -> "FOCUS"
    ActionOptionType.GOAL -> "GOAL"
}
