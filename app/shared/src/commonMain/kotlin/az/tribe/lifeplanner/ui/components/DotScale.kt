package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

/**
 * A one-to-ten answer as ten circles, filled up to the score.
 *
 * The interaction the rest of setup is being rebuilt around: one tap answers a question, the answer
 * is visible without reading anything, and there is no wrong way to use it. Extracted so the wheel
 * and everything else that asks for a number out of ten behave identically rather than drifting.
 *
 * [color] is supplied by the caller because what a number *means* differs: on the wheel a low score
 * is bad, but on a stress scale a low score is good, and colouring both the same way would tell one
 * of those users the opposite of the truth.
 */
@Composable
fun DotScale(
    score: Double?,
    onRate: (Double) -> Unit,
    color: (Double) -> Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp()),
    ) {
        (1..10).forEach { value ->
            val filled = score != null && value <= score
            val cellColor by animateColorAsState(
                if (filled && score != null) color(score)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f),
                label = "dotScaleCell",
            )
            Box(
                Modifier
                    .weight(1f)
                    // Aspect ratio rather than a fixed height: ten across a narrow phone would
                    // otherwise squash into ovals.
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(cellColor)
                    .clickable { onRate(value.toDouble()) },
                contentAlignment = Alignment.Center,
            ) {
                // Only the ends carry a number. Ten of them in a row is a ruler, not a scale.
                if (value == 1 || value == 10) {
                    Text(
                        "$value",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filled) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
