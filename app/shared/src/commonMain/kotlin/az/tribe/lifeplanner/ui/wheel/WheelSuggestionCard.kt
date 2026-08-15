package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.WheelSuggestion
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * One suggestion for the weakest area, or nothing at all.
 *
 * Rendering nothing is the common case by design. [WheelNudgePicker] stays quiet unless an area is
 * both measured and genuinely behind the rest, so on most days this card is absent — a prompt that
 * appears every time stops being help and becomes furniture.
 *
 * There is no dismiss control, because there is nothing to dismiss: it disappears on its own when
 * the area recovers or the wheel evens out.
 */
@Composable
fun WheelSuggestionCard(
    suggestion: WheelSuggestion?,
    modifier: Modifier = Modifier,
    /** True when other areas share the bottom score, so the header does not claim a solo lowest. */
    lowestIsShared: Boolean = false,
) {
    if (suggestion == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(areaColor(suggestion.area))
                )
                Text(
                    // Named rather than dressed up as a generic tip. The user should be able to see
                    // why this appeared without working it out.
                    text = if (lowestIsShared) "${suggestion.area.displayName} is among your lowest"
                    else "${suggestion.area.displayName} is your lowest",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = LifePlannerDesign.Spacing.xs),
                )
            }

            Text(
                text = suggestion.action,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xxs),
            )

            Text(
                text = suggestion.because,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
