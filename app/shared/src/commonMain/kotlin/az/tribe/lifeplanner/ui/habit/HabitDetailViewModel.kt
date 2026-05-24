package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * D7 — backs the redesigned **Habit Detail** screen. Reactive over the habit + today's status (so a
 * check-in reflects instantly), resolves the linked **goal** ("supports" chain), and loads the last
 * five weeks of check-ins for the consistency heatmap. Check-in / undo go through the canonical
 * [CheckInHabitUseCase] / [UncheckHabitUseCase] so streaks stay correct.
 */
class HabitDetailViewModel(
    private val habitId: String,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val uncheckHabitUseCase: UncheckHabitUseCase,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    /** The number of weeks shown in the consistency grid. */
    private val weeks = 5

    /** Reactive source of truth — re-emits on every check-in/mutation, refreshing history below. */
    private val statusFlow = habitRepository.observeHabitsWithTodayStatus()
        .onEach { loadHistory() }

    val habit: StateFlow<Habit?> =
        statusFlow.map { list -> list.firstOrNull { it.first.id == habitId }?.first }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val doneToday: StateFlow<Boolean> =
        statusFlow.map { list -> list.firstOrNull { it.first.id == habitId }?.second ?: false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** The goal this habit supports (its "why"), resolved from [Habit.linkedGoalId]; null if unlinked. */
    val linkedGoalTitle: StateFlow<String?> =
        habit.map { h -> h?.linkedGoalId?.let { runCatching { goalRepository.getGoalById(it)?.title }.getOrNull() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _completedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    /** Dates (within the last [weeks] weeks) the habit was completed — drives the heatmap. */
    val completedDates: StateFlow<Set<LocalDate>> = _completedDates.asStateFlow()

    private val _completionRate = MutableStateFlow(0f)
    /** 30-day completion rate (0..1) shown in the hero ring. */
    val completionRate: StateFlow<Float> = _completionRate.asStateFlow()

    /** First day of the heatmap grid: the Monday five weeks back, so columns are Mon..Sun. */
    fun gridStart(today: LocalDate = today()): LocalDate =
        today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).minus((weeks - 1) * 7, DateTimeUnit.DAY)

    private fun loadHistory() {
        viewModelScope.launch {
            runCatching {
                val today = today()
                val start = gridStart(today)
                val checkIns = habitRepository.getCheckInsInRange(habitId, start, today)
                _completedDates.value = checkIns.filter { it.completed }.map { it.date }.toSet()
                _completionRate.value = habitRepository.getHabitCompletionRate(habitId, 30)
            }.onFailure { Logger.w("HabitDetailViewModel") { "History load failed: ${it.message}" } }
        }
    }

    fun toggleToday() {
        viewModelScope.launch {
            runCatching {
                if (doneToday.value) {
                    uncheckHabitUseCase(habitId)
                } else {
                    checkInHabitUseCase(habitId)
                    gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
                }
                loadHistory()
            }.onFailure { Logger.w("HabitDetailViewModel") { "Toggle check-in failed: ${it.message}" } }
        }
    }

    fun updateHabit(updated: Habit) {
        viewModelScope.launch {
            runCatching { habitRepository.updateHabit(updated) }
                .onFailure { Logger.w("HabitDetailViewModel") { "Update habit failed: ${it.message}" } }
        }
    }
}
