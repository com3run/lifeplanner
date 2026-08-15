package az.tribe.lifeplanner.ui.foryou

/**
 * The goal-setting cascade: vision, then one 90-day quest, then a weekly review cadence. The
 * pillar cards need history a new user does not have; this ladder needs none, because each step
 * is an input the user can give on day one. The feed shows only the first missing step, so cold
 * start reads as "the app has a method" instead of a pile of setup chores.
 */
object GoalSettingCascade {

    /** Settings key stamped by RetrospectiveViewModel when the user opens a review. Epoch millis. */
    const val LAST_REVIEW_AT_KEY = "last_weekly_review_at"

    /** A goal due within this window counts as the active quarterly quest (90 days plus slack). */
    const val QUEST_WINDOW_DAYS = 100

    /** How often the review invitation returns; also the minimum quest age before the first one. */
    const val REVIEW_CADENCE_DAYS = 7

    enum class Step { VISION, QUEST, WEEKLY_REVIEW }

    /**
     * The single cascade step to invite next, or null when the cascade is satisfied for now.
     *
     * @param valuesEnabled whether the values surface (Becoming) is reachable; when its pillar is
     *   off the cascade starts at the quest step.
     * @param hasActiveValues the user has at least one active life value.
     * @param questAgeDays age in days of the newest active quest goal, null when there is none.
     * @param daysSinceLastReview days since the user last opened the retrospective, null = never.
     */
    fun nextStep(
        valuesEnabled: Boolean,
        hasActiveValues: Boolean,
        questAgeDays: Int?,
        daysSinceLastReview: Int?,
    ): Step? = when {
        valuesEnabled && !hasActiveValues -> Step.VISION
        questAgeDays == null -> Step.QUEST
        // The first review waits until the quest is a week old: there is nothing to look back on
        // the day a goal is created. After that it returns on a weekly cadence.
        daysSinceLastReview == null -> Step.WEEKLY_REVIEW.takeIf { questAgeDays >= REVIEW_CADENCE_DAYS }
        daysSinceLastReview >= REVIEW_CADENCE_DAYS -> Step.WEEKLY_REVIEW
        else -> null
    }
}
