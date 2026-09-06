package az.tribe.lifeplanner.ui.habit

import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.ui.UiText

/** Everything the practice ground renders: the countdown for a timed habit, the rep count for a counted one. */
data class HabitPracticeState(
    val habit: Habit? = null,
    val mode: HabitTrackMode = HabitTrackMode.SINGLE,
    /** Countdown target; 0 for count habits. */
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    /** Reps done today for a count habit, including any tapped before this session. */
    val reps: Int = 0,
    val targetReps: Int = 0,
    val done: Boolean = false,
    val isLoading: Boolean = true,
) {
    /** 0f..1f for the ring. Duration counts down, count counts up; both fill the same way. */
    val progress: Float
        get() = when {
            mode == HabitTrackMode.DURATION && totalSeconds > 0 ->
                ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)
            targetReps > 0 -> (reps.toFloat() / targetReps).coerceIn(0f, 1f)
            else -> 0f
        }
}

sealed interface HabitPracticeAction {
    data object OnBackClick : HabitPracticeAction
    data object OnStartClick : HabitPracticeAction
    data object OnPauseClick : HabitPracticeAction
    data object OnResetClick : HabitPracticeAction
    data object OnAddRepClick : HabitPracticeAction
    data object OnCompleteClick : HabitPracticeAction
}

sealed interface HabitPracticeEvent {
    data object NavigateBack : HabitPracticeEvent
    data class ShowSnackbar(val message: UiText) : HabitPracticeEvent
}
