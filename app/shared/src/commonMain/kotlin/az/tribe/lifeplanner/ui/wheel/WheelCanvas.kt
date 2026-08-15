package az.tribe.lifeplanner.ui.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelScore
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The wheel: nine slices, each filled from the centre in proportion to its score.
 *
 * A slice's *radius* carries the score rather than its angle, so every area keeps the same slice of
 * the circle and the shape of the wheel is what you read. A dented rim means an unbalanced life,
 * which is the entire point of the instrument.
 *
 * Areas we could only estimate are drawn hollow, so a guess never passes for a reading.
 */
@Composable
fun WheelCanvas(
    scores: List<WheelScore>,
    onAreaTap: (WheelArea) -> Unit,
    modifier: Modifier = Modifier,
    selected: WheelArea? = null,
    /** Fires continuously while a slice is dragged. For live feedback only, never persisted. */
    onScoreDrag: ((WheelArea, Double) -> Unit)? = null,
    /** Fires once on release, with the value to keep. */
    onScoreCommit: ((WheelArea, Double) -> Unit)? = null,
    /**
     * Fires when a drag is abandoned rather than released: a system gesture taking over, the app
     * backgrounding, the pointer being cancelled. Without it the live value stays on screen and the
     * wheel shows a number the user never chose and cannot get rid of.
     */
    onScoreDragCancel: (() -> Unit)? = null,
    /**
     * Drops the labels and numbers. Below roughly 160dp there is no room for nine labels around a
     * circle: they overlap each other and spill past the wheel into whatever sits beside it. The
     * shape still reads at that size, which is the part worth keeping on a feed.
     */
    compact: Boolean = false,
    /**
     * Where the moved areas stood at the comparison date, drawn as a dashed arc over the current
     * fill. Change on a wheel is a change of shape, so showing the old edge says it in one look
     * where a list of numbers needs reading. Only areas that actually moved belong here; a ghost
     * sitting exactly under the fill is noise.
     */
    ghost: Map<WheelArea, Double> = emptyMap(),
) {
    val segments = remember(scores) { scores.filter { it.area.isWheelSegment }.sortedBy { it.area.order } }
    val measurer = rememberTextMeasurer()

    // The slice being dragged and where the finger has taken it. Held here rather than pushed
    // through the repository on every frame: a database write per pointer event would stutter, and
    // an abandoned drag would leave a value behind that the user never chose.
    var dragging by remember { mutableStateOf<WheelArea?>(null) }
    var dragScore by remember { mutableStateOf(0.0) }

    // Held via rememberUpdatedState so the pointerInput below can key on something stable.
    // Keying it on the callbacks themselves looked harmless and was not: they are fresh lambdas
    // on every recomposition, onScoreDrag causes a recomposition on every frame of a drag, so the
    // gesture detector was torn down and restarted continuously and onDragEnd never arrived. The
    // wheel moved under the finger and then discarded the value.
    val currentDrag by rememberUpdatedState(onScoreDrag)
    val currentCommit by rememberUpdatedState(onScoreCommit)
    val currentCancel by rememberUpdatedState(onScoreDragCancel)
    val canAdjust = onScoreCommit != null

    Box(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
        Canvas(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                .pointerInput(segments) {
                    detectTapGestures { tap ->
                        hitTest(tap, size.width.toFloat(), size.height.toFloat(), segments.size)
                            ?.let { onAreaTap(segments[it].area) }
                    }
                }
                .pointerInput(segments, canAdjust) {
                    if (!canAdjust) return@pointerInput
                    // After a long press, not on plain drag. The wheel lives inside a LazyColumn,
                    // and Compose gives a gesture to whoever claims the pointer first: the scroll
                    // container wins on touch slop every time, so a plain drag detector here never
                    // fired at all. The wheel looked adjustable and silently was not. Waiting for
                    // the long press resolves the contest before the scroll can take it, and keeps
                    // dragging past the wheel working as scrolling.
                    detectDragGesturesAfterLongPress(
                        onDragStart = { start ->
                            // The slice is chosen once, at the start. Tracking the angle during the
                            // drag would hand the score to whichever slice the finger wandered over.
                            dragging = hitTest(start, size.width.toFloat(), size.height.toFloat(), segments.size)
                                ?.let { segments[it].area }
                            dragging?.let {
                                dragScore = scoreAt(start, size.width.toFloat(), size.height.toFloat())
                                currentDrag?.invoke(it, dragScore)
                            }
                        },
                        onDragEnd = {
                            dragging?.let { currentCommit?.invoke(it, dragScore) }
                            dragging = null
                        },
                        onDragCancel = {
                            dragging = null
                            currentCancel?.invoke()
                        },
                    ) { change, _ ->
                        dragging?.let { area ->
                            change.consume()
                            dragScore = scoreAt(change.position, size.width.toFloat(), size.height.toFloat())
                            currentDrag?.invoke(area, dragScore)
                        }
                    }
                }
        ) {
            if (segments.isEmpty()) return@Canvas
            drawWheel(segments, measurer, selected, compact, ghost)
        }
    }
}

/**
 * Distance from the centre as a 0..10 score. Continuous rather than snapped to halves: during a
 * drag the slice should follow the finger exactly, and rounding only happens when the value is kept.
 */
private fun scoreAt(point: Offset, width: Float, height: Float): Double {
    val cx = width / 2f
    val cy = height / 2f
    val rim = minOf(cx, cy) * RIM_FRACTION
    val dx = point.x - cx
    val dy = point.y - cy
    return ((sqrt(dx * dx + dy * dy) / rim) * 10.0).coerceIn(0.0, 10.0)
}

/** Which slice a tap landed in, or null when it fell outside the wheel. */
private fun hitTest(tap: Offset, width: Float, height: Float, count: Int): Int? {
    if (count == 0) return null
    val cx = width / 2f
    val cy = height / 2f
    val radius = minOf(cx, cy) * RIM_FRACTION
    val dx = tap.x - cx
    val dy = tap.y - cy
    if (sqrt(dx * dx + dy * dy) > radius) return null

    // atan2 gives -PI..PI from the positive x-axis; shift so 0 is the top, matching the drawing.
    val degrees = (atan2(dy, dx) * 180.0 / PI + 90.0 + 360.0) % 360.0
    return (degrees / (360.0 / count)).toInt().coerceIn(0, count - 1)
}

private const val RIM_FRACTION = 0.72f
private const val LABEL_FRACTION = 0.86f

private fun DrawScope.drawWheel(
    segments: List<WheelScore>,
    measurer: TextMeasurer,
    selected: WheelArea?,
    compact: Boolean,
    ghost: Map<WheelArea, Double>,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rim = minOf(cx, cy) * RIM_FRACTION
    val sweep = 360f / segments.size
    val topLeft = Offset(cx - rim, cy - rim)
    val boxSize = Size(rim * 2, rim * 2)

    // The rim first, so every slice sits inside a complete circle and a low score reads as a gap
    // rather than as a missing piece of the diagram.
    drawCircle(color = RimColor, radius = rim, center = Offset(cx, cy), style = Stroke(width = 2f))

    segments.forEachIndexed { index, score ->
        val start = index * sweep - 90f
        val filled = (score.score / 10.0).toFloat().coerceIn(0f, 1f)
        val sliceRadius = rim * filled
        val color = areaColor(score.area)

        if (score.source == ScoreSource.ESTIMATED) {
            // A guess: outlined at its own radius, not the rim. Drawing it full-size made every
            // unknown area read as a perfect 10 while its number said 5.
            drawArc(
                color = color.copy(alpha = 0.5f),
                startAngle = start,
                sweepAngle = sweep - SliceGap,
                useCenter = true,
                topLeft = Offset(cx - sliceRadius, cy - sliceRadius),
                size = Size(sliceRadius * 2, sliceRadius * 2),
                style = Stroke(width = 2f),
            )
        } else if (sliceRadius > 0f) {
            drawArc(
                color = color,
                startAngle = start,
                sweepAngle = sweep - SliceGap,
                useCenter = true,
                topLeft = Offset(cx - sliceRadius, cy - sliceRadius),
                size = Size(sliceRadius * 2, sliceRadius * 2),
            )
        }

        // Where the slice used to reach, always drawn as the muted region so "then" reads the
        // same way in both directions.
        //
        // A rise and a drop need opposite treatments, which is not obvious until you see it fail.
        // A drop leaves its old extent outside the fill, sitting on the dark background, so a
        // translucent tint of the area colour reads fine. A rise leaves its old extent *inside*
        // the fill, and a translucent tint of the same colour over that same colour is simply that
        // colour: the first attempt drew it and it was perfectly invisible. So a rise darkens
        // instead, and the old core shows through the brighter fill.
        ghost[score.area]?.let { previous ->
            val previousRadius = rim * (previous / 10.0).toFloat().coerceIn(0f, 1f)
            val grew = previousRadius < sliceRadius
            if (kotlin.math.abs(previousRadius - sliceRadius) > 1f) {
                if (grew) {
                    drawArc(
                        color = Color.Black.copy(alpha = 0.32f),
                        startAngle = start,
                        sweepAngle = sweep - SliceGap,
                        useCenter = true,
                        topLeft = Offset(cx - previousRadius, cy - previousRadius),
                        size = Size(previousRadius * 2, previousRadius * 2),
                    )
                } else {
                    // An annulus sector: Canvas has no ring primitive, so stroking the mid-radius
                    // with a width equal to the gap fills exactly the region between the two.
                    val midRadius = (sliceRadius + previousRadius) / 2f
                    drawArc(
                        color = color.copy(alpha = 0.30f),
                        startAngle = start,
                        sweepAngle = sweep - SliceGap,
                        useCenter = false,
                        topLeft = Offset(cx - midRadius, cy - midRadius),
                        size = Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = previousRadius - sliceRadius),
                    )
                }
                // The old edge itself, so where it stood stays legible when the gap is thin.
                drawArc(
                    color = color.copy(alpha = 0.85f),
                    startAngle = start,
                    sweepAngle = sweep - SliceGap,
                    useCenter = false,
                    topLeft = Offset(cx - previousRadius, cy - previousRadius),
                    size = Size(previousRadius * 2, previousRadius * 2),
                    style = Stroke(width = 1.5f),
                )
            }
        }

        if (score.area == selected) {
            drawArc(
                color = color,
                startAngle = start,
                sweepAngle = sweep - SliceGap,
                useCenter = true,
                topLeft = topLeft,
                size = boxSize,
                style = Stroke(width = 3f),
            )
        }

        if (!compact) {
            drawSliceLabels(score, start + (sweep - SliceGap) / 2f, cx, cy, rim, sliceRadius, measurer)
        }
    }
}

private fun DrawScope.drawSliceLabels(
    score: WheelScore,
    midAngleDegrees: Float,
    cx: Float,
    cy: Float,
    rim: Float,
    sliceRadius: Float,
    measurer: TextMeasurer,
) {
    val radians = midAngleDegrees * PI / 180.0

    val labelRadius = minOf(cx, cy) * LABEL_FRACTION
    val lx = cx + (cos(radians) * labelRadius).toFloat()
    val ly = cy + (sin(radians) * labelRadius).toFloat()
    val label = measurer.measure(
        text = score.area.displayName,
        style = TextStyle(fontSize = 11.sp, color = LabelColor, textAlign = TextAlign.Center),
    )
    drawText(label, topLeft = Offset(lx - label.size.width / 2f, ly - label.size.height / 2f))

    // The number rides inside its own slice, so it stays legible against the fill. A slice too
    // short to hold it gets the number just outside instead of a clipped one inside.
    val insideEnough = sliceRadius > rim * 0.42f
    val numberRadius = if (insideEnough) sliceRadius * 0.62f else sliceRadius + rim * 0.14f
    val nx = cx + (cos(radians) * numberRadius).toFloat()
    val ny = cy + (sin(radians) * numberRadius).toFloat()
    val number = measurer.measure(
        text = formatScore(score.score),
        style = TextStyle(
            fontSize = 13.sp,
            color = if (insideEnough) Color.White else LabelColor,
        ),
    )
    drawText(number, topLeft = Offset(nx - number.size.width / 2f, ny - number.size.height / 2f))
}

/** Halves show as "9.5", whole numbers as "9". */
internal fun formatScore(score: Double): String {
    val whole = score.toInt()
    return if (score == whole.toDouble()) "$whole" else "$whole.5"
}

private val SliceGap = 1.5f
private val RimColor = Color(0xFFD9D2C4)
private val LabelColor = Color(0xFF8A8A8A)

/**
 * Slice colours follow the reference wheel's grouping rather than a rainbow: the outward-facing
 * areas warm, the inward-facing ones cool, so neighbouring slices read as related.
 */
internal fun areaColor(area: WheelArea): Color = when (area) {
    WheelArea.MISSION -> Color(0xFF35D0EA)
    WheelArea.FAMILY -> Color(0xFFF5C518)
    WheelArea.FRIENDS -> Color(0xFFD9A400)
    WheelArea.ROMANCE -> Color(0xFFFAE08A)
    WheelArea.SPIRITUAL -> Color(0xFFF29AA8)
    WheelArea.MENTAL -> Color(0xFFD9536B)
    WheelArea.PHYSICAL -> Color(0xFFF2C2CB)
    WheelArea.GROWTH -> Color(0xFF2AAFA0)
    WheelArea.MONEY -> Color(0xFF12808C)
    WheelArea.JOY -> Color(0xFF2FB574)
}

