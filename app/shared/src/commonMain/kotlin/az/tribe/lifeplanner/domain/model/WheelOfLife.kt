package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory

/**
 * The Wheel of Life: ten areas, each scored out of ten.
 *
 * Deliberately separate from [LifeArea] and [GoalCategory]. Those measure **engagement** (do you
 * have goals here, are you keeping the habits), which is why an account with no goals scores a flat
 * 14 everywhere. The wheel measures how good the area actually **is**, which is a different question
 * and needs its own scale.
 *
 * Scores are predicted from the user's own data so the wheel arrives filled in rather than as ten
 * blank sliders, then the user adjusts anything we read wrong (see [WheelScore.source]). [rubric]
 * is what makes a prediction meaningful: it fixes what a 10 means, so the number the app suggests
 * and the number the user would have picked are on the same scale.
 *
 * [categories] is the soft link back to the stable goal categories, used only to find the signal
 * for a prediction and to suggest a goal for a low area. Nothing about a goal or habit changes.
 */
enum class WheelArea(
    val order: Int,
    val displayName: String,
    /** What a 10 looks like. Shown to the user next to the slider, and the anchor the predictor aims at. */
    val rubric: String,
    val emoji: String,
    val categories: List<GoalCategory>,
) {
    MISSION(
        order = 1,
        displayName = "Mission",
        rubric = "Your work matters to you and you can say why, in a sentence, without hesitating.",
        emoji = "🎯",
        categories = listOf(GoalCategory.CAREER),
    ),
    FAMILY(
        order = 2,
        displayName = "Family",
        rubric = "Nothing with your family is sitting unresolved, whether you are close to them or keep your distance.",
        emoji = "🏡",
        categories = listOf(GoalCategory.FAMILY),
    ),
    FRIENDS(
        order = 3,
        displayName = "Friends",
        rubric = "Someone knows what you are actually dealing with right now, not the version of you from a year ago.",
        emoji = "👥",
        categories = listOf(GoalCategory.PEOPLE),
    ),
    ROMANCE(
        order = 4,
        displayName = "Romance",
        rubric = "Your romantic life is where you want it, whether that means a good relationship or a contented single one.",
        emoji = "💞",
        categories = emptyList(),
    ),
    SPIRITUAL(
        order = 5,
        displayName = "Spiritual",
        rubric = "You have a practice that puts your problems in proportion, and you actually do it.",
        emoji = "🧘",
        categories = listOf(GoalCategory.PURPOSE),
    ),
    MENTAL(
        order = 6,
        displayName = "Mental",
        rubric = "Your head is a decent place to spend the day. Hard days pass instead of settling in.",
        emoji = "🧠",
        categories = listOf(GoalCategory.WELLBEING),
    ),
    PHYSICAL(
        order = 7,
        displayName = "Physical",
        rubric = "You move most days, sleep properly, and your body is not the thing holding you back.",
        emoji = "💪",
        categories = listOf(GoalCategory.BODY),
    ),
    GROWTH(
        order = 8,
        displayName = "Growth",
        rubric = "You are better at something than you were six months ago, and you could name what.",
        emoji = "🌱",
        categories = listOf(GoalCategory.CAREER),
    ),
    MONEY(
        order = 9,
        displayName = "Money",
        rubric = "Money is not a daily worry. An unexpected bill would be annoying rather than frightening.",
        emoji = "💰",
        categories = listOf(GoalCategory.MONEY),
    ),

    /**
     * Joy sits outside the wheel in the original, as a reading of the whole rather than a tenth
     * slice, and it keeps that role here: [WheelScorePredictor] derives it from the other areas
     * plus recorded mood instead of from goals of its own.
     */
    JOY(
        order = 10,
        displayName = "Joy",
        rubric = "You enjoyed something this week for no reason other than that you enjoy it.",
        emoji = "✨",
        categories = emptyList(),
    );

    /** True when the wheel draws this as a slice. Joy is rendered apart, as in the original. */
    val isWheelSegment: Boolean get() = this != JOY

    companion object {
        fun sorted(): List<WheelArea> = entries.sortedBy { it.order }

        /** The nine slices, Joy excluded. */
        fun segments(): List<WheelArea> = sorted().filter { it.isWheelSegment }
    }
}

/** Where a score came from. A user's own number always outranks ours. */
enum class ScoreSource {
    /** The user set it themselves. Never overwritten by a later prediction. */
    USER,

    /** Predicted from the user's data, with enough signal to be worth showing. */
    PREDICTED,

    /** Predicted, but from little or no signal. Worth asking the user to confirm. */
    ESTIMATED,
}

/**
 * One area's score, 0..10 in halves (the original uses 9.5), mirroring what the wheel draws.
 */
data class WheelScore(
    val area: WheelArea,
    /** 0.0..10.0, rounded to the nearest half. */
    val score: Double,
    val source: ScoreSource,
    /**
     * 0.0..1.0. How much data stood behind a prediction. Always 1.0 for [ScoreSource.USER].
     * Drives the "confirm this?" prompt rather than any scoring maths.
     */
    val confidence: Double,
    /** Plain-language reason for the number, e.g. "3 habits going, longest streak 21 days". */
    val basis: String,
) {
    init {
        require(score in 0.0..10.0) { "score out of range: $score" }
        require(confidence in 0.0..1.0) { "confidence out of range: $confidence" }
    }

    /** True when we guessed and the user has not weighed in. */
    val needsConfirmation: Boolean
        get() = source == ScoreSource.ESTIMATED
}

/**
 * A full wheel. [overall] is the mean of the nine segments; Joy is reported alongside rather than
 * folded in, so a good mood cannot paper over a thin wheel.
 */
data class WheelReport(
    val id: String,
    val scores: List<WheelScore>,
    val generatedAt: kotlinx.datetime.LocalDateTime,
) {
    val segments: List<WheelScore> get() = scores.filter { it.area.isWheelSegment }

    val joy: WheelScore? get() = scores.firstOrNull { it.area == WheelArea.JOY }

    /** Mean of the nine slices, to one decimal. Joy excluded by design. */
    val overall: Double
        get() = segments.takeIf { it.isNotEmpty() }
            ?.let { areas -> (areas.sumOf { it.score } / areas.size).roundToHalf() }
            ?: 0.0

    val lowest: WheelScore? get() = segments.minByOrNull { it.score }

    val highest: WheelScore? get() = segments.maxByOrNull { it.score }

    /** Areas we guessed at and would like the user to confirm, worst-guessed first. */
    val unconfirmed: List<WheelScore>
        get() = scores.filter { it.needsConfirmation }.sortedBy { it.confidence }

    /**
     * The spread between best and worst slice. The point of the wheel is its shape, so a flat 6
     * everywhere is a different situation from 9s beside 3s even though the mean matches.
     */
    val spread: Double
        get() = segments.takeIf { it.isNotEmpty() }
            ?.let { (it.maxOf { s -> s.score } - it.minOf { s -> s.score }) }
            ?: 0.0
}

/** The wheel is drawn in halves, so every score snaps to the same grid. */
fun Double.roundToHalf(): Double {
    val snapped = kotlin.math.round(this * 2) / 2
    return snapped.coerceIn(0.0, 10.0)
}
