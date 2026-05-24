package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.LifePlannerGradients
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * D7, shared premium building blocks so the redesigned screens match the app's existing gradient/
 * depth craft bar (not flat). Token-aware: gradients come from [LifePlannerGradients], spacing/radius
 * from [LifePlannerDesign].
 */

/** A rounded gradient hero header, the premium banner pattern used across the home. White content. */
@Composable
fun GradientHero(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    gradient: Brush = LifePlannerGradients.primary,
    eyebrow: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.extraLarge))
            .background(gradient)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(
                        eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (trailing != null) trailing()
        }
    }
}

/** An icon inside a soft tinted rounded square, the "colored icon chip" used in list rows/cards. */
// (icon-chip helper below)
@Composable
fun IconChip(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    boxSize: Dp = 44.dp,
) {
    Box(
        modifier
            .size(boxSize)
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.small))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(boxSize * 0.5f))
    }
}
