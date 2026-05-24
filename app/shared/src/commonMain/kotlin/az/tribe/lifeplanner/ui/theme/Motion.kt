package az.tribe.lifeplanner.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * D10 — the motion system. Calm by default, alive on purpose (D1 P6): motion is reserved for
 * transitions that aid understanding and for tactile feedback, never decoration. Durations + easing
 * are tokens so every animation in the app shares one feel.
 */
object Motion {
    /** Durations in ms. fast = micro feedback, medium = transitions, slow = hero/celebration. */
    object Duration {
        const val fast = 120
        const val medium = 240
        const val slow = 400
    }

    /** Standard easing for most transitions (Material's decelerate). */
    val standard: Easing = FastOutSlowInEasing

    /** Emphasized easing for entrances / important moves — a confident settle. */
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** How far a pressed, tappable surface scales down — subtle, tactile. */
    const val pressScale = 0.97f
}

/**
 * A tactile replacement for [clickable] on cards/rows: the surface scales down slightly while
 * pressed (no ripple), giving a physical, modern feel. Use on the design-system cards.
 */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.pressScale else 1f,
        animationSpec = tween(durationMillis = Motion.Duration.fast, easing = Motion.standard),
        label = "pressScale",
    )
    return this
        .scale(scale)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}
