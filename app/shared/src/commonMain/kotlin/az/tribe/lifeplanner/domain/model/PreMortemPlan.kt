package az.tribe.lifeplanner.domain.model

/**
 * Crystal Ball (Pillars 3/4/7) — one pre-committed implementation intention, written by the
 * user on the CRYSTAL_BALL step of the goal wizard: "a reason I won't follow through" plus the
 * if-then fix they will run when it happens.
 *
 * Resurfacing: when a Choice Point fires for [goalId] with a matching [triggerType], the
 * Choice Point sheet should lead with [thenAction] ("You planned for this: …") and bump
 * [timesSurfaced] / [timesActedOn], which later feed the TuningInferenceEngine.
 *
 * @param triggerType a `ChoicePointTrigger` name (HABIT_STREAK_BREAK / GOAL_STALLED /
 *   DEADLINE_PASSED) or "OTHER" for obstacles the detector can't watch for.
 */
data class PreMortemPlan(
    val id: String,
    val goalId: String,
    val obstacle: String,
    val ifCondition: String,
    val thenAction: String,
    val triggerType: String,
    val timesSurfaced: Int = 0,
    val timesActedOn: Int = 0,
    val createdAt: String = "",
)
