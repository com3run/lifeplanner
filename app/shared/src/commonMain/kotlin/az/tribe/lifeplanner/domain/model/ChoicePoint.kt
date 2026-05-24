package az.tribe.lifeplanner.domain.model

enum class ChoicePointTrigger { HABIT_STREAK_BREAK, GOAL_STALLED, DEADLINE_PASSED }

/**
 * Pillar 3, an ephemeral (non-persisted) prompt raised by [ChoicePointDetector] when
 * something would otherwise silently roll over (a broken streak, a stalled goal, a passed
 * deadline). The user resolves it with a deliberate re-choice that's recorded as a [Decision].
 */
data class ChoicePoint(
    val trigger: ChoicePointTrigger,
    val title: String,
    val prompt: String,
    val subjectTitle: String,
    val relatedGoalId: String? = null,
    val relatedHabitId: String? = null
)
