package az.tribe.lifeplanner.domain.model

/**
 * One point on the life-balance trajectory. [weekOffset] is relative to now: negative is the past,
 * 0 is this week, positive is projected weeks ahead. [score] is a 0..100 balance score.
 */
data class TrajectoryPoint(val weekOffset: Int, val score: Float)

/**
 * The four lines the user explores on the trajectory graph:
 * - [past]: how it's gone (reconstructed from real activity),
 * - [currentPace]: how it's going to be if nothing changes (recent trend continued),
 * - [couldBe]: how it could be with more effort (driven by the explore slider),
 * - [ideal]: the target to aim for.
 * [projectedEndScore] is where [couldBe] lands at the horizon, the headline number the slider moves.
 */
data class TrajectorySeries(
    val past: List<TrajectoryPoint>,
    val currentPace: List<TrajectoryPoint>,
    val couldBe: List<TrajectoryPoint>,
    val ideal: List<TrajectoryPoint>,
    val projectedEndScore: Int,
)
