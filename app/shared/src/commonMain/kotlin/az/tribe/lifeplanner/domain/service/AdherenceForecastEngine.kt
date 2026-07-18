package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.TuningDial
import kotlin.math.roundToInt

/**
 * Pillar 4 x Pillar 7 — the "chance you'll actually follow through" forecast shown on the
 * FORECAST step of the goal wizard (Crystal Ball feature).
 *
 * Pure domain logic, no platform/framework deps — mirrors [CalibrationEngine] / [TuningInferenceEngine].
 * It combines two things almost no other planner has in one place:
 *   1. the user's own [DecisionProfile] wiring dials (each nudge scaled by that dial's *confidence*,
 *      so a barely-sampled dial barely moves the number), and
 *   2. their predicted-vs-actual [Calibration] history (passed in as a ratio + sample count).
 *
 * Honesty is a design constraint: with little evidence it returns a wide band, a LOW confidence,
 * and `isColdStart = true` rather than a false-precise number. It never claims a driver it can't back.
 *
 * ## Wiring it up (caller side)
 * ```
 * val cal = calibrationProvider.calibration()               // Calibration? (null if < 3 finished goals)
 * val forecast = AdherenceForecastEngine().forecast(
 *     profile = decisionProfileRepository.current(),         // DecisionProfile?
 *     plan = GoalPlan(
 *         horizonDays = draft.dueDate?.let { today.daysUntil(it) } ?: 90,
 *         majorMoveCount = draft.milestones.size + draft.habits.size,
 *         hasNearTermWins = draft.milestones.any { it.estimatedEffort <= LOW || it.dueSoon(today) },
 *         mitigationCount = crystalBall.fixes.size,           // 0 until the CRYSTAL_BALL step
 *     ),
 *     calibrationRatio = cal?.ratio,
 *     calibrationSamples = cal?.sampleSize ?: 0,
 * )
 * ```
 * The engine deliberately does **not** import the Goal or Calibration types — the caller maps into
 * [GoalPlan] + primitives, keeping this testable and decoupled.
 */
class AdherenceForecastEngine(
    /** Population prior for follow-through, used directly on cold start. */
    private val baseRate: Double = 0.45,
) {

    fun forecast(
        profile: DecisionProfile?,
        plan: GoalPlan,
        calibrationRatio: Double? = null,
        calibrationSamples: Int = 0,
    ): AdherenceForecast {
        val drivers = mutableListOf<ForecastDriver>()

        val calFactor = calibrationFactor(calibrationRatio, calibrationSamples, drivers)
        val dialF = if (profile != null) dialFactor(profile, plan, drivers) else 1.0
        val loadF = loadFactor(plan, drivers)
        val mitigationF = 1.0 + MITIGATION_STEP * plan.mitigationCount.coerceIn(0, MAX_MITIGATIONS)

        val odds = (baseRate * calFactor * dialF * loadF * mitigationF).coerceIn(FLOOR, CEIL)

        val evidence = evidenceStrength(profile, calibrationSamples)
        val confidence = when {
            evidence >= HIGH_EVIDENCE -> ForecastConfidence.HIGH
            evidence >= MED_EVIDENCE -> ForecastConfidence.MEDIUM
            else -> ForecastConfidence.LOW
        }
        val band = when (confidence) {
            ForecastConfidence.HIGH -> BAND_HIGH
            ForecastConfidence.MEDIUM -> BAND_MED
            ForecastConfidence.LOW -> BAND_LOW
        }

        return AdherenceForecast(
            adherencePercent = (odds * 100).roundToInt(),
            bandPercent = band,
            confidence = confidence,
            drivers = drivers.sortedByDescending { it.magnitude }.take(MAX_DRIVERS),
            isColdStart = evidence < MED_EVIDENCE,
        )
    }

    /** Does this user historically land near their own predicted finish date? */
    private fun calibrationFactor(ratio: Double?, samples: Int, out: MutableList<ForecastDriver>): Double {
        if (ratio == null || samples < CALIBRATION_MIN_SAMPLES) return 1.0
        return when {
            ratio > SLIP_THRESHOLD -> {
                val f = (1.0 / ratio).coerceIn(0.70, 1.0) // the more it slips, the lower
                out += ForecastDriver(
                    dial = null,
                    direction = ForecastDriver.Direction.LOWERS,
                    magnitude = (1.0 - f).toFloat().coerceIn(0f, 1f),
                    explanation = "Your goals usually run longer than you predict, so this one's odds are trimmed.",
                )
                f
            }
            ratio < EARLY_THRESHOLD -> {
                out += ForecastDriver(null, ForecastDriver.Direction.RAISES, 0.10f,
                    "You tend to finish ahead of your own estimates.")
                1.08
            }
            else -> {
                out += ForecastDriver(null, ForecastDriver.Direction.RAISES, 0.08f,
                    "You usually finish about when you predict — that reliability counts in your favour.")
                1.06
            }
        }
    }

    private fun dialFactor(profile: DecisionProfile, plan: GoalPlan, out: MutableList<ForecastDriver>): Double {
        var factor = 1.0

        // DELAY_DISCOUNTING high + long horizon: wants the payoff now, this goal pays off late -> down.
        profile.delayDiscounting.let { d ->
            val impatience = pole(d) // -1..1, >0 = wants payoff sooner
            val horizonPull = (plan.horizonDays / LONG_HORIZON_DAYS).coerceIn(0.0, 1.0)
            if (impatience > 0.0) {
                val nudge = impatience * horizonPull * d.confidence * DELAY_WEIGHT
                factor *= (1.0 - nudge)
                addDial(out, TuningDial.DELAY_DISCOUNTING, ForecastDriver.Direction.LOWERS, nudge, d,
                    "You lean toward near-term payoffs, and this goal mostly pays off in about ${(plan.horizonDays / 30).coerceAtLeast(1)} months.")
            }
        }

        // PUNISHMENT_SENSITIVITY high: setbacks land hard -> likelier to quit after a miss -> down.
        // (This is exactly the factor the Crystal Ball pre-mortem is meant to blunt.)
        profile.punishmentSensitivity.let { d ->
            val sensitivity = pole(d)
            if (sensitivity > 0.0) {
                val nudge = sensitivity * d.confidence * PUNISHMENT_WEIGHT
                factor *= (1.0 - nudge)
                addDial(out, TuningDial.PUNISHMENT_SENSITIVITY, ForecastDriver.Direction.LOWERS, nudge, d,
                    "Setbacks tend to land hard for you — one rough week can end a streak. The Crystal Ball step guards against this.")
            }
        }

        // REWARD_SENSITIVITY high + near-term wins in the plan: early dopamine -> up.
        profile.rewardSensitivity.let { d ->
            val reward = pole(d)
            if (reward > 0.0 && plan.hasNearTermWins) {
                val nudge = reward * d.confidence * REWARD_WEIGHT
                factor *= (1.0 + nudge)
                addDial(out, TuningDial.REWARD_SENSITIVITY, ForecastDriver.Direction.RAISES, nudge, d,
                    "You respond strongly to wins, and this plan front-loads a few quick ones.")
            }
        }

        // RISK_AVERSION low (risk-tolerant) + a heavy plan: tends to over-commit -> down.
        profile.riskAversion.let { d ->
            val tolerance = -pole(d) // >0 = risk-tolerant
            if (tolerance > 0.0 && plan.majorMoveCount >= MANY_MOVES) {
                val nudge = tolerance * d.confidence * RISK_WEIGHT
                factor *= (1.0 - nudge)
                addDial(out, TuningDial.RISK_AVERSION, ForecastDriver.Direction.LOWERS, nudge, d,
                    "You tend to take on a lot at once, which can spread you thin across ${plan.majorMoveCount} moves.")
            }
        }

        return factor
    }

    /** Too many moves for the time given lowers sustainability. */
    private fun loadFactor(plan: GoalPlan, out: MutableList<ForecastDriver>): Double {
        if (plan.horizonDays <= 0 || plan.majorMoveCount <= 0) return 1.0
        val movesPerMonth = plan.majorMoveCount / (plan.horizonDays / 30.0)
        if (movesPerMonth <= SUSTAINABLE_MOVES_PER_MONTH) return 1.0
        val over = (movesPerMonth - SUSTAINABLE_MOVES_PER_MONTH).coerceAtMost(5.0)
        val nudge = (over * LOAD_WEIGHT).coerceAtMost(0.25)
        out += ForecastDriver(null, ForecastDriver.Direction.LOWERS, nudge.toFloat(),
            "That's a lot to sustain at once for the window you've set.")
        return 1.0 - nudge
    }

    /** 0..1 — how much behaviour we actually have to go on (dial confidence + calibration history). */
    private fun evidenceStrength(profile: DecisionProfile?, calibrationSamples: Int): Double {
        val dialEvidence = profile?.dials?.values?.map { it.confidence.toDouble() }?.average() ?: 0.0
        val calEvidence = (calibrationSamples / 10.0).coerceIn(0.0, 1.0)
        return (0.7 * dialEvidence + 0.3 * calEvidence).coerceIn(0.0, 1.0)
    }

    /** Dial position as a signed pole in -1..1 (0 = neutral midpoint). */
    private fun pole(d: DialSetting): Double = ((d.value - DialSetting.NEUTRAL) * 2f).toDouble()

    private fun addDial(
        out: MutableList<ForecastDriver>,
        dial: TuningDial,
        direction: ForecastDriver.Direction,
        nudge: Double,
        setting: DialSetting,
        text: String,
    ) {
        // Only surface a driver we can actually stand behind.
        if (setting.confidence >= SURFACE_CONFIDENCE && nudge >= SURFACE_NUDGE) {
            out += ForecastDriver(dial, direction, nudge.toFloat().coerceIn(0f, 1f), text)
        }
    }

    companion object {
        // Hand-tuned constants (PRD open question) — safe to move behind Remote Config later.
        private const val FLOOR = 0.05
        private const val CEIL = 0.95
        private const val CALIBRATION_MIN_SAMPLES = 3 // matches CalibrationEngine(minSamples = 3)
        private const val SLIP_THRESHOLD = 1.25
        private const val EARLY_THRESHOLD = 0.85
        private const val LONG_HORIZON_DAYS = 120.0
        private const val MANY_MOVES = 4
        private const val DELAY_WEIGHT = 0.30
        private const val PUNISHMENT_WEIGHT = 0.22
        private const val REWARD_WEIGHT = 0.15
        private const val RISK_WEIGHT = 0.15
        private const val LOAD_WEIGHT = 0.05
        private const val SUSTAINABLE_MOVES_PER_MONTH = 4.0
        private const val MITIGATION_STEP = 0.04
        private const val MAX_MITIGATIONS = 3
        private const val MAX_DRIVERS = 2
        private const val HIGH_EVIDENCE = 0.60
        private const val MED_EVIDENCE = 0.30
        private const val BAND_HIGH = 6
        private const val BAND_MED = 12
        private const val BAND_LOW = 20
        private const val SURFACE_CONFIDENCE = 0.25
        private const val SURFACE_NUDGE = 0.02
    }
}

/** The minimal shape of the draft plan the forecast needs. Caller maps the Goal into this. */
data class GoalPlan(
    /** Days from now until the goal's due date. */
    val horizonDays: Int,
    /** Milestones + habits the user has committed to ("major moves"). */
    val majorMoveCount: Int,
    /** True if any move lands within ~2 weeks (early wins). */
    val hasNearTermWins: Boolean,
    /** Number of Crystal Ball if-then fixes written (raises adherence). 0 until the CRYSTAL_BALL step. */
    val mitigationCount: Int = 0,
)

/** The forecast rendered on the FORECAST wizard step and (P1) the Goal Detail odds chip. */
data class AdherenceForecast(
    /** 0..100 — "chance YOU specifically follow through". */
    val adherencePercent: Int,
    /** +/- width around [adherencePercent]; wider = less certain. */
    val bandPercent: Int,
    val confidence: ForecastConfidence,
    /** Top drivers, largest magnitude first (empty when there's nothing we can justify). */
    val drivers: List<ForecastDriver>,
    /** True when we're mostly guessing — the UI should say "we'll sharpen this as we learn you". */
    val isColdStart: Boolean,
)

enum class ForecastConfidence { LOW, MEDIUM, HIGH }

/** One plain-English reason the number moved, for the "why" line under the forecast. */
data class ForecastDriver(
    /** The wiring dial behind this driver, or null for non-dial drivers (calibration, plan load). */
    val dial: TuningDial?,
    val direction: Direction,
    /** 0..1 — how much this moved the estimate; used to rank which drivers to show. */
    val magnitude: Float,
    val explanation: String,
) {
    enum class Direction { RAISES, LOWERS }
}
