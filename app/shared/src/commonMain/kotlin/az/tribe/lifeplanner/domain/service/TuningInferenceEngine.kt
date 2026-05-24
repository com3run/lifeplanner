package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.BehaviorWindow
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.TuningDial
import kotlin.math.min

/**
 * Pillar 7 (Innate), pure, on-device inference of the user's six [TuningDial]s from observed
 * behaviour. NO AI, NO self-report: true to *Innate*'s point that it's the individual trajectory,
 * not a questionnaire, that reveals the wiring. Deterministic over its input → fully unit-testable.
 *
 * Each dial is estimated as an **evidence-weighted running mean**: this window's behaviour is
 * blended with the prior profile in proportion to their sample sizes, so a dial moves only as fast
 * as evidence accrues and its [DialSetting.confidence] grows with the total observation count.
 * Feed [infer] successive *delta* windows to accumulate evidence over time.
 *
 * No dial value is "good" or "bad", the engine describes the user, it never grades them.
 */
class TuningInferenceEngine(
    /** Total observations at which a dial reaches full confidence (1.0). */
    private val saturationSamples: Int = 20,
) {
    private data class Evidence(val value: Float, val n: Int)

    private val noEvidence = Evidence(DialSetting.NEUTRAL, 0)

    /**
     * Blend [window] into [prior] (or a neutral profile when null) and return the updated profile.
     * Reuses the prior's id, or [newProfileId] when starting fresh.
     */
    fun infer(prior: DecisionProfile?, window: BehaviorWindow, newProfileId: String): DecisionProfile {
        var profile = DecisionProfile(id = prior?.id ?: newProfileId)
        for (dial in TuningDial.entries) {
            val priorSetting = prior?.dial(dial) ?: DialSetting()
            profile = profile.withDial(dial, blend(priorSetting, evidenceFor(dial, window)))
        }
        return profile
    }

    private fun evidenceFor(dial: TuningDial, w: BehaviorWindow): Evidence = when (dial) {
        // Acting quickly on what's surfaced ⇒ a LOW confidence threshold, so invert the ratio.
        TuningDial.CONFIDENCE_THRESHOLD ->
            ratio(w.suggestionsActedOnQuickly, w.suggestionsSurfaced)
                ?.let { Evidence(1f - it, w.suggestionsSurfaced) } ?: noEvidence

        TuningDial.NOVELTY_SALIENCE ->
            ratio(w.distinctCategoriesStarted, w.goalsStarted)
                ?.let { Evidence(it, w.goalsStarted) } ?: noEvidence

        // Two sub-signals: leaning to short-horizon goals, and cutting focus sessions short.
        TuningDial.DELAY_DISCOUNTING -> combine(
            ratio(w.shortHorizonGoals, w.shortHorizonGoals + w.longHorizonGoals)
                ?.let { Evidence(it, w.shortHorizonGoals + w.longHorizonGoals) },
            w.avgFocusActualOverPlanned
                ?.takeIf { w.focusSessions > 0 }
                ?.let { Evidence((1f - it).coerceIn(0f, 1f), w.focusSessions) },
        )

        TuningDial.PUNISHMENT_SENSITIVITY ->
            ratio(w.abandonmentsAfterMiss, w.habitMisses)
                ?.let { Evidence(it, w.habitMisses) } ?: noEvidence

        TuningDial.REWARD_SENSITIVITY ->
            ratio(w.reengagementsAfterReward, w.rewardEvents)
                ?.let { Evidence(it, w.rewardEvents) } ?: noEvidence

        TuningDial.RISK_AVERSION ->
            ratio(w.safeGoalsChosen, w.safeGoalsChosen + w.ambitiousGoalsChosen)
                ?.let { Evidence(it, w.safeGoalsChosen + w.ambitiousGoalsChosen) } ?: noEvidence
    }

    /** numerator ÷ denominator clamped to 0..1, or null when there's no evidence. */
    private fun ratio(num: Int, den: Int): Float? =
        if (den <= 0) null else (num.toFloat() / den).coerceIn(0f, 1f)

    /** Fold several sub-signals into one evidence-weighted (value, n). */
    private fun combine(vararg parts: Evidence?): Evidence {
        val present = parts.filterNotNull().filter { it.n > 0 }
        val totalN = present.sumOf { it.n }
        if (totalN == 0) return noEvidence
        val value = present.sumOf { (it.value.toDouble() * it.n) }.toFloat() / totalN
        return Evidence(value, totalN)
    }

    /** Evidence-weighted running mean of prior and new evidence; confidence grows with total n. */
    private fun blend(prior: DialSetting, evidence: Evidence): DialSetting {
        val totalN = prior.sampleSize + evidence.n
        if (totalN == 0) return DialSetting() // never any evidence → neutral, zero confidence
        val value = (prior.value * prior.sampleSize + evidence.value * evidence.n) / totalN
        return DialSetting(
            value = value.coerceIn(0f, 1f),
            confidence = min(1f, totalN.toFloat() / saturationSamples),
            sampleSize = totalN,
        )
    }
}
