package az.tribe.lifeplanner.ui.focus

import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED, CANCELLED
}

sealed class FocusEvent {
    data class SessionCompleted(val xpEarned: Int, val durationMinutes: Int) : FocusEvent()
    data class SessionCancelled(val partialXp: Int) : FocusEvent()

    /** A habit linked to Focus was completed by this session, so the user can be told. */
    data class HabitCredited(val habitTitle: String, val xpEarned: Int) : FocusEvent()
}

data class MilestoneItem(
    val milestone: Milestone,
    val goal: Goal
)
