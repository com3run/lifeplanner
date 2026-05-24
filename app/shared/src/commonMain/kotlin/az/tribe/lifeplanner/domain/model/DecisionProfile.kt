package az.tribe.lifeplanner.domain.model

/**
 * Pillar 7 (Innate), the user's "wiring". Kevin Mitchell's *Innate* argues personality
 * bottoms out in a few independently-tuned decision-making parameters (Fig 6.2). The app
 * tunes *itself* to these dials rather than asking the user to fight their own nature.
 *
 * Each dial is a continuum. The pole names below are **descriptive, not evaluative**, no
 * value is "good" or "bad", and neither end is the failure case. [DecisionProfile] describes
 * the user; it never grades them.
 */
enum class TuningDial {
    /** Certainty needed before acting. Low = decisive / quick to commit, high = deliberate / wants to be sure. */
    CONFIDENCE_THRESHOLD,

    /** Pull toward the new. Low = prefers routine and continuity, high = seeks variety and novelty. */
    NOVELTY_SALIENCE,

    /** How steeply future payoffs are discounted. Low = patient / long-horizon, high = wants the payoff now. */
    DELAY_DISCOUNTING,

    /** Weight of setbacks and missed targets. Low = shrugs off misses, high = strongly affected by them. */
    PUNISHMENT_SENSITIVITY,

    /** Pull of wins and positive feedback. Low = largely indifferent to rewards, high = strongly reward-driven. */
    REWARD_SENSITIVITY,

    /** Stance toward uncertain outcomes. Low = risk-tolerant / experimental, high = risk-averse / prefers the safe option. */
    RISK_AVERSION,
}

/**
 * One dial's inferred setting.
 *
 * @param value the position on the dial, `0f..1f`, with `0.5f` the neutral midpoint. Neither
 *   pole is preferable, see the matching [TuningDial] entry for what each end describes.
 * @param confidence how sure the inference is, `0f..1f`. Stays low until enough behaviour has
 *   accrued, so the app can be honest about how little it may yet know the user.
 * @param sampleSize the number of behavioural observations behind the inference.
 */
data class DialSetting(
    val value: Float = NEUTRAL,
    val confidence: Float = 0f,
    val sampleSize: Int = 0,
) {
    /** True once there is enough evidence to surface this setting without hedging. */
    val isReliable: Boolean
        get() = confidence >= RELIABLE_CONFIDENCE && sampleSize >= MIN_SAMPLES

    companion object {
        const val NEUTRAL = 0.5f
        const val RELIABLE_CONFIDENCE = 0.5f
        const val MIN_SAMPLES = 10
    }
}

/**
 * The user's six tuning dials, inferred from behaviour over time (never a self-report survey),
 * maintained on-device, and fed into the Possibility ranking (Pillar 2), Choice Points (Pillar 3),
 * the Causal Model (Pillar 4), and the Becoming layer (Pillar 5). One profile per user.
 *
 * Pure domain model: no platform or framework dependencies.
 */
data class DecisionProfile(
    val id: String,
    val confidenceThreshold: DialSetting = DialSetting(),
    val noveltySalience: DialSetting = DialSetting(),
    val delayDiscounting: DialSetting = DialSetting(),
    val punishmentSensitivity: DialSetting = DialSetting(),
    val rewardSensitivity: DialSetting = DialSetting(),
    val riskAversion: DialSetting = DialSetting(),
) {
    /** The six dials keyed by [TuningDial], for uniform iteration (inference, UI). */
    val dials: Map<TuningDial, DialSetting>
        get() = mapOf(
            TuningDial.CONFIDENCE_THRESHOLD to confidenceThreshold,
            TuningDial.NOVELTY_SALIENCE to noveltySalience,
            TuningDial.DELAY_DISCOUNTING to delayDiscounting,
            TuningDial.PUNISHMENT_SENSITIVITY to punishmentSensitivity,
            TuningDial.REWARD_SENSITIVITY to rewardSensitivity,
            TuningDial.RISK_AVERSION to riskAversion,
        )

    /** Read one dial by enum, so callers (ranking, Choice Points) can stay dial-agnostic. */
    fun dial(dial: TuningDial): DialSetting = dials.getValue(dial)

    /** Replace one dial, returning an updated profile. Keeps the data class immutable. */
    fun withDial(dial: TuningDial, setting: DialSetting): DecisionProfile = when (dial) {
        TuningDial.CONFIDENCE_THRESHOLD -> copy(confidenceThreshold = setting)
        TuningDial.NOVELTY_SALIENCE -> copy(noveltySalience = setting)
        TuningDial.DELAY_DISCOUNTING -> copy(delayDiscounting = setting)
        TuningDial.PUNISHMENT_SENSITIVITY -> copy(punishmentSensitivity = setting)
        TuningDial.REWARD_SENSITIVITY -> copy(rewardSensitivity = setting)
        TuningDial.RISK_AVERSION -> copy(riskAversion = setting)
    }

    companion object {
        /** A fresh, fully-neutral profile, every dial at the midpoint with zero confidence. */
        fun neutral(id: String): DecisionProfile = DecisionProfile(id = id)
    }
}
