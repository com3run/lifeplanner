package az.tribe.lifeplanner.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.ActionOption
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.PossibilityContextProvider
import az.tribe.lifeplanner.domain.service.PossibilityEngine
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * D7, backs the redesigned **Today** agency surface (D2). Now wired to the real **Pillar 2**
 * `PossibilityEngine` (via [PossibilityContextProvider]), "Right now you could…" is the genuine
 * ranked [ActionOption]s, not a heuristic. Today's habits + inline check-in stay here.
 */
class TodayViewModel(
    private val habitRepository: HabitRepository,
    private val contextProvider: PossibilityContextProvider,
    private val engine: PossibilityEngine,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    /** Today's habits with their done-status, the daily check-in surface. */
    val habitsToday: StateFlow<List<HabitToday>> =
        habitRepository.observeHabitsWithTodayStatus()
            .map { list -> list.map { (h, done) -> HabitToday(h, done) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Today's plan: the concrete things scheduled for today, the incomplete milestones due today or
     * overdue across active goals. A compact planner strip on the Today screen, soonest first.
     */
    val todayPlan: StateFlow<List<PlanItem>> =
        goalRepository.observeAllGoals()
            .map { goals ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                goals.asSequence()
                    .filter { it.status != GoalStatus.COMPLETED }
                    .flatMap { g ->
                        g.milestones.asSequence()
                            .filter { !it.isCompleted && (it.dueDate?.let { d -> d <= today } == true) }
                            .map { m -> PlanItem(g.id, g.title, m.title, m.dueDate!!, m.dueDate!! < today) }
                    }
                    .sortedBy { it.dueDate }
                    .toList()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** "Right now you could…", ranked options from the Pillar 2 engine. */
    private val _possibilities = MutableStateFlow<List<ActionOption>>(emptyList())
    val possibilities: StateFlow<List<ActionOption>> = _possibilities.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _possibilities.value = try {
                engine.rank(contextProvider.currentContext())
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
}

/** A habit plus whether it's done today. */
data class HabitToday(val habit: Habit, val doneToday: Boolean)

/** One scheduled item on today's plan: a milestone due today/overdue, with its parent goal. */
data class PlanItem(
    val goalId: String,
    val goalTitle: String,
    val title: String,
    val dueDate: LocalDate,
    val overdue: Boolean,
)
