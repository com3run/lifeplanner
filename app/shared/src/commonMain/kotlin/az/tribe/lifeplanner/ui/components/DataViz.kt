package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.Motion
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * D8, the data-visualization system. Every number is a **mirror, not a verdict** (D1 P2): viz is
 * calm, honest about uncertainty, and explains *why*. Token-pure; animations use [Motion].
 */

/** Animated circular progress. [progress] is 0f..1f; optional [content] sits in the center. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 56.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.modernColors.primary,
    trackColor: Color = MaterialTheme.modernColors.surfaceVariant,
    content: (@Composable () -> Unit)? = null,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(Motion.Duration.slow, easing = Motion.emphasized),
        label = "progressRing",
    )
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(diameter)) {
            val sw = strokeWidth.toPx()
            val arc = Size(size.width - sw, size.height - sw)
            val topLeft = Offset(sw / 2, sw / 2)
            drawArc(trackColor, 0f, 360f, false, topLeft = topLeft, size = arc, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * animated, false, topLeft = topLeft, size = arc, style = Stroke(sw, cap = StrokeCap.Round))
        }
        content?.invoke()
    }
}

/**
 * Oura-style insight card: a plain-language [headline], the optional *why* ([detail]), and an
 * honest [confidenceLabel] (e.g. "Still learning · 6 signals" / "High confidence"). Never a verdict.
 */
@Composable
fun InsightCard(
    headline: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    confidenceLabel: String? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.modernColors.primary,
) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) IconChip(icon, tint = accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(headline, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.modernColors.textPrimary)
                if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.textSecondary)
                if (confidenceLabel != null) {
                    Text(confidenceLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textTertiary)
                }
            }
        }
    }
}

/** A compact stat: a big [value] with a [label] and an optional [accent]-colored value. */
@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.modernColors.textPrimary,
) {
    Surface(modifier, color = MaterialTheme.modernColors.cardBackground, shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
        }
    }
}
