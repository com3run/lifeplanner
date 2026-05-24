package az.tribe.lifeplanner.domain.model

/**
 * Pillar 7 (Innate) — aggregated behavioural signals over one observation window, the input to
 * [az.tribe.lifeplanner.domain.service.TuningInferenceEngine]. Built by a provider from the user's
 * own history (focus sessions, habit check-ins, goals, suggestion interactions) — never a survey.
 *
 * Each pair of fields is a "did it / out of how many" tally that leans toward the **high** pole of
 * one [TuningDial]; the denominator is the evidence behind that lean. A zero denominator means no
 * evidence this window — the dial keeps its prior value.
 *
 * A window should cover only **new** observations since the prior profile was computed, so the
 * engine can accumulate evidence incrementally.
 */
data class BehaviorWindow(
    // CONFIDENCE_THRESHOLD — acting fast on a surfaced option signals a LOW threshold.
    val suggestionsSurfaced: Int = 0,
    val suggestionsActedOnQuickly: Int = 0,

    // NOVELTY_SALIENCE — variety across what the user chooses to start.
    val goalsStarted: Int = 0,
    val distinctCategoriesStarted: Int = 0,

    // DELAY_DISCOUNTING — preference for near-term payoff, plus cutting focus sessions short.
    val shortHorizonGoals: Int = 0,
    val longHorizonGoals: Int = 0,
    val focusSessions: Int = 0,
    /** Mean (actual ÷ planned) focus length over the window: 1.0 = ran the full plan, <1 = cut short. */
    val avgFocusActualOverPlanned: Float? = null,

    // PUNISHMENT_SENSITIVITY — reaction to a miss: drop the streak vs. carry on.
    val habitMisses: Int = 0,
    val abandonmentsAfterMiss: Int = 0,

    // REWARD_SENSITIVITY — re-engaging right after a win (XP / badge / streak).
    val rewardEvents: Int = 0,
    val reengagementsAfterReward: Int = 0,

    // RISK_AVERSION — choosing the safe option over the stretch one.
    val safeGoalsChosen: Int = 0,
    val ambitiousGoalsChosen: Int = 0,
)
