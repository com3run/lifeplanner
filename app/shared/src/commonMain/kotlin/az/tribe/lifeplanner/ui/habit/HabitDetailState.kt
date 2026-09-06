package az.tribe.lifeplanner.ui.habit

import androidx.compose.runtime.Stable
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.UiText
import kotlinx.datetime.LocalDate

/** Everything the Habit Detail screen renders. Lists and sets make it unstable by default, hence [Stable]. */
@Stable
data class HabitDetailState(
    val habit: Habit? = null,
    /** True until the first emission from the store, so a real habit never flashes as "not found". */
    val isLoading: Boolean = true,
    val doneToday: Boolean = false,
    /** The goal this habit supports, resolved from [Habit.linkedGoalId]; null when unlinked. */
    val linkedGoalTitle: String? = null,
    /** The day the heatmap ends on; the ViewModel owns the clock so the screen stays previewable. */
    val today: LocalDate? = null,
    /** Dates within the heatmap window the habit was completed. */
    val completedDates: Set<LocalDate> = emptySet(),
    /** 0..100, the last 30 days of check-ins. */
    val completionRate: Float = 0f,
    /** Lessons about this habit, ranked against what it is about. */
    val relatedLessons: List<KnowledgeBit> = emptyList(),
    val isEditing: Boolean = false,
)

sealed interface HabitDetailAction {
    data object OnBackClick : HabitDetailAction
    data object OnToggleTodayClick : HabitDetailAction
    data object OnEditClick : HabitDetailAction
    data object OnEditDismiss : HabitDetailAction
    data class OnEditConfirm(val habit: Habit) : HabitDetailAction
    data object OnPracticeClick : HabitDetailAction
    data class OnLessonClick(val lessonId: String) : HabitDetailAction
}

sealed interface HabitDetailEvent {
    data object NavigateBack : HabitDetailEvent
    data class NavigateToLesson(val lessonId: String) : HabitDetailEvent
    data class NavigateToPractice(val habitId: String) : HabitDetailEvent
    data class ShowSnackbar(val message: UiText) : HabitDetailEvent
}
