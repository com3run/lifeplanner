package az.tribe.lifeplanner.ui.wheel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * What a score out of ten looks like, warm at the bottom and cool at the top.
 *
 * The wheel's own colours are per **area** — Money is always gold, Mental always rose — so that a
 * slice is recognisable at a glance regardless of how it is going. That is the right choice there
 * and the wrong one on a rating scale, where the only thing worth seeing is whether the number is
 * good or bad.
 *
 * A continuous ramp rather than five bands, so 6 and 7 are visibly different and the bar shifts as
 * you drag across it instead of jumping.
 *
 * Deliberately not red-for-anything-under-five. A 4 is an ordinary part of an ordinary life, and
 * colouring half the scale like an alarm tells people their life is failing when they were being
 * honest with us. Red is reserved for the bottom, where someone is genuinely struggling.
 */
internal fun scoreColor(score: Double): Color {
    val s = score.coerceIn(1.0, 10.0)
    return when {
        s <= 3.0 -> lerp(Red, Orange, ((s - 1.0) / 2.0).toFloat())
        s <= 5.0 -> lerp(Orange, Amber, ((s - 3.0) / 2.0).toFloat())
        s <= 7.0 -> lerp(Amber, Green, ((s - 5.0) / 2.0).toFloat())
        else -> lerp(Green, Teal, ((s - 7.0) / 3.0).toFloat())
    }
}

private val Red = Color(0xFFE5484D)
private val Orange = Color(0xFFF76B15)
private val Amber = Color(0xFFF5C518)
private val Green = Color(0xFF46B860)
private val Teal = Color(0xFF25B8C4)
