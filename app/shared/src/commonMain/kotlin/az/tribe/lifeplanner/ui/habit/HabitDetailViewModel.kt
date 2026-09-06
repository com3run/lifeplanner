package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.ui.UiText
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.RecommendLessonsForHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import leanlifeplanner.app.shared.generated.resources.Res
import leanlifeplanner.app.shared.generated.resources.xp_earned

/**
 * Backs the **Habit Detail** screen. One [HabitDetailState] holds everything the screen shows;
 * the screen sends [HabitDetailAction]s and reacts to one-shot [HabitDetailEvent]s.
 *
 * Reactive over the habit and today's status (so a check-in reflects instantly), resolves the
 * linked goal ("supports" chain), and loads the last five weeks of check-ins for the consistency
 * heatmap. Check-in and undo go through the canonical [CheckInHabitUseCase] and
 * [UncheckHabitUseCase] so streaks stay correct.
 */
class HabitDetailViewModel(
    private val habitId: String,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val uncheckHabitUseCase: UncheckHabitUseCase,
    private val awardHabitCompletionUseCase: AwardHabitCompletionUseCase,
    private val recommendLessonsForHabit: RecommendLessonsForHabitUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailState())
    val state: StateFlow<HabitDetailState> = _state.asStateFlow()

    private val _events = Channel<HabitDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            habitRepository.observeHabitsWithTodayStatus().collect { habits ->
                val entry = habits.firstOrNull { it.first.id == habitId }
                val previous = _state.value.habit
                _state.update {
                    it.copy(habit = entry?.first, doneToday = entry?.second ?: false, isLoading = false)
                }
                loadHistory()
                val habit = entry?.first
                if (habit != null && habit != previous) {
                    loadLinkedGoal(habit)
                    loadRelatedLessons(habit)
                }
            }
        }
    }

    fun onAction(action: HabitDetailAction) {
        when (action) {
            HabitDetailAction.OnBackClick -> send(HabitDetailEvent.NavigateBack)
            HabitDetailAction.OnToggleTodayClick -> toggleToday()
            HabitDetailAction.OnEditClick -> _state.update { it.copy(isEditing = true) }
            HabitDetailAction.OnEditDismiss -> _state.update { it.copy(isEditing = false) }
            is HabitDetailAction.OnEditConfirm -> updateHabit(action.habit)
            HabitDetailAction.OnPracticeClick ->
                _state.value.habit?.let { send(HabitDetailEvent.NavigateToPractice(it.id)) }
            is HabitDetailAction.OnLessonClick -> send(HabitDetailEvent.NavigateToLesson(action.lessonId))
        }
    }

    private fun send(event: HabitDetailEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    /** First day of the heatmap grid: the Monday five weeks back, so columns are Mon..Sun. */
    private fun gridStart(today: LocalDate): LocalDate =
        today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).minus((WEEKS - 1) * 7, DateTimeUnit.DAY)

    private suspend fun loadLinkedGoal(habit: Habit) {
        val title = habit.linkedGoalId?.let { id ->
            runCatching { goalRepository.getGoalById(id)?.title }.getOrNull()
        }
        _state.update { it.copy(linkedGoalTitle = title) }
    }

    private suspend fun loadRelatedLessons(habit: Habit) {
        val lessons = runCatching { recommendLessonsForHabit(habit, RELATED_LESSON_COUNT) }
            .getOrDefault(emptyList())
        _state.update { it.copy(relatedLessons = lessons) }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            runCatching {
                val today = today()
                val checkIns = habitRepository.getCheckInsInRange(habitId, gridStart(today), today)
                val rate = habitRepository.getHabitCompletionRate(habitId, 30)
                _state.update {
                    it.copy(
                        today = today,
                        completedDates = checkIns.filter { c -> c.completed }.map { c -> c.date }.toSet(),
                        completionRate = rate,
                    )
                }
            }.onFailure { Logger.w(TAG) { "History load failed: ${it.message}" } }
        }
    }

    private fun toggleToday() {
        viewModelScope.launch {
            runCatching {
                if (_state.value.doneToday) {
                    uncheckHabitUseCase(habitId)
                } else {
                    val today = today()
                    checkInHabitUseCase(habitId, today)
                    val xp = awardHabitCompletionUseCase(habitId, today)
                    if (xp > 0) {
                        _events.send(
                            HabitDetailEvent.ShowSnackbar(UiText.StringResource(Res.string.xp_earned, arrayOf(xp)))
                        )
                    }
                }
                loadHistory()
            }.onFailure { Logger.w(TAG) { "Toggle check-in failed: ${it.message}" } }
        }
    }

    private fun updateHabit(updated: Habit) {
        viewModelScope.launch {
            runCatching { habitRepository.updateHabit(updated) }
                .onFailure { Logger.w(TAG) { "Update habit failed: ${it.message}" } }
            _state.update { it.copy(isEditing = false) }
        }
    }

    private companion object {
        const val TAG = "HabitDetailViewModel"

        /** The number of weeks shown in the consistency grid. */
        const val WEEKS = 5

        /** Enough to feel like a shelf, few enough to stay a sidebar rather than a screen. */
        const val RELATED_LESSON_COUNT = 3
    }
}
