package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDate

/**
 * A wheel as it stood on one day.
 *
 * Snapshots exist because a past wheel **cannot be recomputed**. Predictions read trailing windows
 * (a week of steps, a month of mood), so the evidence behind last month's score has aged out by the
 * time you want to look back at it. Recomputing would silently answer "what would today's signals
 * say about those areas", which is a different question. Every day without a snapshot is a day of
 * history that cannot be recovered, so [WheelRepository.captureSnapshot] runs on open.
 */
data class WheelSnapshot(
    val date: LocalDate,
    val scores: Map<WheelArea, Double>,
) {
    val overall: Double
        get() = WheelArea.segments().mapNotNull { scores[it] }
            .takeIf { it.isNotEmpty() }
            ?.let { (it.sum() / it.size).roundToHalf() }
            ?: 0.0
}

/** One area's movement between two snapshots. */
data class WheelDelta(
    val area: WheelArea,
    val from: Double,
    val to: Double,
) {
    // Not roundToHalf(): that clamps to 0..10 because it rounds *scores*. Applied to a difference
    // it turns every drop into "no change", so a falling area would silently never be reported.
    val change: Double get() = roundHalfSigned(to - from)

    val rose: Boolean get() = change > 0.0
    val fell: Boolean get() = change < 0.0
}

/** Snaps to the same half-point grid the wheel draws, but keeps the sign. */
internal fun roundHalfSigned(value: Double): Double = kotlin.math.round(value * 2) / 2

/** Which past wheel to measure against. */
enum class ComparisonPeriod(val displayName: String, val days: Int) {
    DAY("since yesterday", 1),
    WEEK("this week", 7),
    MONTH("this month", 30),
}

/**
 * Two wheels and what moved between them.
 *
 * [previous] is the nearest snapshot at or before the target date rather than an exact match, since
 * a user who did not open the app on the exact day should still get a comparison. [previousDate]
 * reports which day was actually used so the UI can say so instead of implying precision.
 */
data class WheelComparison(
    val period: ComparisonPeriod,
    val previousDate: LocalDate,
    val currentDate: LocalDate,
    val deltas: List<WheelDelta>,
    /**
     * Areas with a score now that had none then.
     *
     * Deliberately not deltas: a first-ever score is not a gain, and counting it as one would
     * invent a trend out of the user answering a question. But they cannot be silent either.
     * Score three areas for the first time and a comparison built only from deltas reports the
     * wheel as unchanged, which reads as the feature being broken rather than as it being careful.
     */
    val newlyScored: List<WheelArea> = emptyList(),
) {
    /** Biggest gains first. */
    val risen: List<WheelDelta> get() = deltas.filter { it.rose }.sortedByDescending { it.change }

    /** Biggest drops first. */
    val fallen: List<WheelDelta> get() = deltas.filter { it.fell }.sortedBy { it.change }

    val unchanged: List<WheelDelta> get() = deltas.filter { it.change == 0.0 }

    val overallChange: Double
        get() = deltas.filter { it.area.isWheelSegment }
            .takeIf { it.isNotEmpty() }
            ?.let { list -> roundHalfSigned(list.sumOf { it.change } / list.size) }
            ?: 0.0

    /** The single movement worth leading with: the largest, in whichever direction. */
    val headline: WheelDelta?
        get() = deltas.maxByOrNull { kotlin.math.abs(it.change) }?.takeIf { it.change != 0.0 }

    val hasMovement: Boolean get() = deltas.any { it.change != 0.0 }

    /** True when there is anything at all to report, movement or a first score. */
    val hasSomethingToSay: Boolean get() = hasMovement || newlyScored.isNotEmpty()

    /** The previous wheel, for drawing over the current one. Only areas that actually moved. */
    val movedFrom: Map<WheelArea, Double>
        get() = deltas.filter { it.change != 0.0 }.associate { it.area to it.from }
}

/**
 * Builds a comparison from two snapshots. Areas missing from either side are skipped rather than
 * treated as zero: an area we had no snapshot for did not drop to zero, we simply do not know.
 */
fun compareWheels(
    period: ComparisonPeriod,
    previous: WheelSnapshot,
    current: WheelSnapshot,
): WheelComparison = WheelComparison(
    period = period,
    previousDate = previous.date,
    currentDate = current.date,
    deltas = WheelArea.sorted().mapNotNull { area ->
        val from = previous.scores[area] ?: return@mapNotNull null
        val to = current.scores[area] ?: return@mapNotNull null
        WheelDelta(area, from, to)
    },
    newlyScored = WheelArea.sorted().filter { it in current.scores && it !in previous.scores },
)
