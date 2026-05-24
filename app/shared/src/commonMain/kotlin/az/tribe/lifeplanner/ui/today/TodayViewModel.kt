package az.tribe.lifeplanner.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * D7 — backs the redesigned **Today** agency surface (D2): "what can I do right now?". Sources only
 * the data available on `main` today (habits + goals); the "Right now you could…" list uses an
 * interim on-device heuristic ([buildPossibilities]) with a **clean seam**: when the Pillar 2
 * `PossibilityEngine` lands on `main`, swap that one function for it — the screen contract is
 * already `List<Possibility>`.
 */
class TodayViewModel(
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    /** Today's habits with their done-status, the daily check-in surface. */
    val habitsToday: StateFlow<List<HabitToday>> =
        habitRepository.observeHabitsWithTodayStatus()
            .map { list -> list.map { (h, done) -> HabitToday(h, done) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _possibilities = MutableStateFlow<List<Possibility>>(emptyList())
    val possibilities: StateFlow<List<Possibility>> = _possibilities.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _possibilities.value = try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val habits = habitRepository.getHabitsWithTodayStatus(today)
                val goals = goalRepository.getActiveGoals()
                buildPossibilities(habits, goals)
            } catch (e: Exception) {
                Logger.w("TodayViewModel") { "Possibility build failed: ${e.message}" }
                emptyList()
            }
        }
    }

    fun checkInHabit(habitId: String) {
        viewModelScope.launch {
            runCatching {
                habitRepository.checkIn(habitId, Clock.System.todayIn(TimeZone.currentSystemDefault()))
                refresh()
            }.onFailure { Logger.w("TodayViewModel") { "Check-in failed: ${it.message}" } }
        }
    }

    /**
     * INTERIM heuristic — replaced by the Pillar 2 `PossibilityEngine` when it reaches `main`.
     * Surfaces up to 3 "you could…" options with a plain fit reason: an undone habit, then the
     * goals nearest needing attention. Deliberately simple and honest (no fake ranking).
     */
    private fun buildPossibilities(habits: List<Pair<Habit, Boolean>>, goals: List<Goal>): List<Possibility> {
        val out = mutableListOf<Possibility>()
        habits.firstOrNull { (_, done) -> !done }?.let { (h, _) ->
            out += Possibility(h.title, "A quick win — you haven't done this today.", Possibility.Kind.HABIT, h.id)
        }
        goals.filter { it.status != GoalStatus.COMPLETED }
            .sortedBy { it.dueDate }
            .take(2)
            .forEach { g -> out += Possibility(g.title, "Move this goal forward.", Possibility.Kind.GOAL, g.id) }
        return out.take(3)
    }
}

/** A habit plus whether it's done today. */
data class HabitToday(val habit: Habit, val doneToday: Boolean)

/** An ephemeral "right now you could…" suggestion (interim; see [TodayViewModel.buildPossibilities]). */
data class Possibility(
    val title: String,
    val reason: String,
    val kind: Kind,
    val targetId: String,
) {
    enum class Kind { HABIT, GOAL, FOCUS }
}
