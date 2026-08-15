package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.TrajectoryPoint
import az.tribe.lifeplanner.domain.model.TrajectorySeries
import kotlin.math.roundToInt

/**
 * Turns a real past into three forward-looking lines the user can explore. Pure and platform-free
 * so it's fully unit-testable and recomputes cheaply as the "effort" slider moves.
 *
 * - current pace: the recent trend (least-squares slope over the past) simply continued,
 * - could be: that trend plus extra lift proportional to [improvement] (0..1) and the headroom to
 *   ideal, so full effort would close the gap to [idealScore] by the horizon,
 * - ideal: a flat target line.
 *
 * All scores are clamped to 0..100.
 */
object TrajectoryProjector {

    fun project(
        past: List<TrajectoryPoint>,
        weeksAhead: Int,
        improvement: Float,
        idealScore: Float,
    ): TrajectorySeries {
        val anchor = past.lastOrNull() ?: TrajectoryPoint(weekOffset = 0, score = 50f)
        // Cap the extrapolated trend: a steep recent stretch shouldn't linearly rocket a balance
        // score to ~100. Keeps "current pace" and "could be" believable.
        val slope = weeklySlope(past).coerceIn(-MAX_SLOPE, MAX_SLOPE)

        val currentPace = (0..weeksAhead).map { w ->
            TrajectoryPoint(anchor.weekOffset + w, clamp(anchor.score + slope * w))
        }

        // Extra weekly lift so that at full effort the couldBe line reaches ideal by the horizon.
        val headroom = (idealScore - anchor.score).coerceAtLeast(0f)
        val extraPerWeek = if (weeksAhead > 0) improvement.coerceIn(0f, 1f) * (headroom / weeksAhead) else 0f
        val couldSlope = maxOf(slope, 0f) + extraPerWeek
        val couldBe = (0..weeksAhead).map { w ->
            TrajectoryPoint(anchor.weekOffset + w, clamp(anchor.score + couldSlope * w))
        }

        val ideal = (0..weeksAhead).map { w -> TrajectoryPoint(anchor.weekOffset + w, idealScore) }

        return TrajectorySeries(
            past = past,
            currentPace = currentPace,
            couldBe = couldBe,
            ideal = ideal,
            projectedEndScore = (couldBe.lastOrNull()?.score ?: anchor.score).roundToInt(),
        )
    }

    /** Least-squares slope (score change per week) over the past points; 0 if fewer than 2. */
    private fun weeklySlope(points: List<TrajectoryPoint>): Float {
        if (points.size < 2) return 0f
        val n = points.size
        val meanX = points.sumOf { it.weekOffset.toDouble() } / n
        val meanY = points.sumOf { it.score.toDouble() } / n
        var num = 0.0
        var den = 0.0
        for (p in points) {
            val dx = p.weekOffset - meanX
            num += dx * (p.score - meanY)
            den += dx * dx
        }
        return if (den == 0.0) 0f else (num / den).toFloat()
    }

    private fun clamp(v: Float): Float = v.coerceIn(0f, 100f)

    /** Max believable weekly change in a balance score, points/week. */
    private const val MAX_SLOPE = 2.5f
}
