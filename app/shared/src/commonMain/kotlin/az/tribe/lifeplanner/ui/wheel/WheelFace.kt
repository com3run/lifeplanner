package az.tribe.lifeplanner.ui.wheel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A face whose expression is a function of the wheel's overall score.
 *
 * Deliberately drawn rather than picked from a set of emoji: the mouth's curvature is
 * `(score - 5) / 5`, so the expression is continuous and cannot be nudged into looking cheerful
 * when the number is not. A 4.5 gets a very slightly downturned mouth, not a brave little smile.
 * That honesty is the point — a face that grinned at a 3 would make the whole instrument feel like
 * flattery, and the user would stop believing the number next to it.
 *
 * At 5 the mouth is a flat line, which reads as neutral rather than unhappy.
 */
@Composable
fun WheelFace(
    score: Double,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF2C2C2A),
) {
    // A spring rather than a tween, because this is dragged as often as it is set. A tween has a
    // fixed duration, so mid-drag it always lags the finger by its remaining time and the face
    // feels detached from the hand. A spring is defined by where it is now and where it is going,
    // so retargeting every frame stays smooth, and the slight overshoot on release is what makes
    // the face read as reacting rather than redrawing.
    val curve by animateFloatAsState(
        targetValue = ((score - 5.0) / 5.0).toFloat().coerceIn(-1f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "faceCurve",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = (w * 0.07f).coerceAtLeast(2f)

        val eyeY = h * 0.38f
        val eyeDx = w * 0.21f
        val eyeR = w * 0.055f

        // Happy eyes arc upward past a strong score; below that they stay simple dots, because
        // drooping eyes on a middling score reads as pity rather than honesty. Same Y convention as
        // the mouth: the control point sits below the ends so the arc bows upward on screen.
        if (curve > 0.55f) {
            val lift = (curve - 0.55f) / 0.45f
            listOf(-eyeDx, eyeDx).forEach { dx ->
                val path = Path().apply {
                    moveTo(w / 2 + dx - eyeR * 1.6f, eyeY + eyeR * 0.5f)
                    quadraticTo(
                        w / 2 + dx, eyeY + eyeR * (0.6f + lift * 1.4f),
                        w / 2 + dx + eyeR * 1.6f, eyeY + eyeR * 0.5f,
                    )
                }
                drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
        } else {
            listOf(-eyeDx, eyeDx).forEach { dx ->
                drawCircle(tint, radius = eyeR, center = Offset(w / 2 + dx, eyeY))
            }
        }

        // The mouth. Canvas Y grows downward, so a smile needs its control point *below* the line
        // joining the corners, not above: an earlier version negated this and drew a frown at every
        // good score. It went unnoticed because a fresh wheel sits at 5, where the mouth is flat.
        val mouthY = h * 0.63f
        val mouthHalf = w * 0.24f
        val control = mouthY + (curve * h * 0.26f)
        val mouth = Path().apply {
            moveTo(w / 2 - mouthHalf, mouthY)
            quadraticTo(w / 2, control, w / 2 + mouthHalf, mouthY)
        }
        drawPath(mouth, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/**
 * A plain-language reading of the wheel, paired with the face.
 *
 * Written to stay level rather than encouraging. A wheel at 3 is described as a hard stretch, not
 * as "room to grow", because the softer phrasing is what makes people distrust the whole screen.
 */
fun wheelMood(score: Double): String = when {
    score >= 9.0 -> "Genuinely good, all round."
    score >= 7.5 -> "Solid. Most of it is working."
    score >= 6.0 -> "Decent, with a couple of thin spots."
    score >= 5.0 -> "Middling. Nothing broken, nothing thriving."
    score >= 3.5 -> "More is off than on right now."
    score >= 2.0 -> "This is a hard stretch."
    else -> "Most of this is not working. Pick one thing."
}
