package az.tribe.lifeplanner.ui.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.ChoicePoint
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.ChoicePointDetector
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Pillar 3, the user's response to a [ChoicePoint]: a deliberate re-choice. */
enum class ChoicePointAction(val label: String) {
    KEEP("Keep"),
    RESCHEDULE("Reschedule"),
    SHRINK("Shrink"),
    DROP("Drop")
}

/**
 * Pillar 3, drives the Decision Journal: the logged [Decision]s plus the currently
 * pending [ChoicePoint]s, and resolves a choice point into a recorded [Decision].
 */
class DecisionViewModel(
    private val decisionRepository: DecisionRepository,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val detector: ChoicePointDetector,
    private val decisionProfileRepository: DecisionProfileRepository,
) : ViewModel() {

    val decisions: StateFlow<List<Decision>> =
        decisionRepository.observeAllDecisions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _choicePoints = MutableStateFlow<List<ChoicePoint>>(emptyList())
    val choicePoints: StateFlow<List<ChoicePoint>> = _choicePoints.asStateFlow()

    init { refreshChoicePoints() }

    fun refreshChoicePoints() {
        viewModelScope.launch {
            _choicePoints.value = try {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val goals = goalRepository.getActiveGoals()
                val habits = habitRepository.observeHabitsWithTodayStatus().firstOrNull().orEmpty()
                val profile = decisionProfileRepository.getProfile() // Pillar 7: tune prompts to the user's wiring
                detector.detect(today, goals, habits, profile)
            } catch (e: Exception) {
                Logger.w("DecisionViewModel") { "Choice-point detection failed: ${e.message}" }
                emptyList()
            }
        }
    }

    suspend fun getDecision(id: String): Decision? = decisionRepository.getDecisionById(id)

    @OptIn(ExperimentalUuidApi::class)
    fun resolve(choicePoint: ChoicePoint, action: ChoicePointAction, reasoning: String) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                decisionRepository.insertDecision(
                    Decision(
                        id = Uuid.random().toString(),
                        question = choicePoint.title,
                        optionsConsidered = ChoicePointAction.entries.map { it.label },
                        chosenOption = action.label,
                        reasoning = reasoning,
                        relatedGoalId = choicePoint.relatedGoalId,
                        expectedOutcome = "",
                        confidence = 50,
                        decidedAt = now
                    )
                )
                applyAction(choicePoint, action)
                refreshChoicePoints()
            } catch (e: Exception) {
                Logger.w("DecisionViewModel") { "Resolving choice point failed: ${e.message}" }
            }
        }
    }

    private suspend fun applyAction(choicePoint: ChoicePoint, action: ChoicePointAction) {
        val goalId = choicePoint.relatedGoalId ?: return
        when (action) {
            ChoicePointAction.DROP -> goalRepository.archiveGoal(goalId)
            ChoicePointAction.RESCHEDULE -> {
                val goal = goalRepository.getGoalById(goalId) ?: return
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                goalRepository.updateGoal(goal.copy(dueDate = today.plus(DatePeriod(days = 14))))
            }
            // KEEP / SHRINK: the deliberate decision is recorded; no structural change here.
            ChoicePointAction.KEEP, ChoicePointAction.SHRINK -> Unit
        }
    }
}
