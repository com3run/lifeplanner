package az.tribe.lifeplanner.ui.possibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.model.Possibility
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.usecases.CreateGoalUseCase
import az.tribe.lifeplanner.usecases.GeneratePossibilitiesUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 6 — backs Possibility Mode. Divergence (TRI-44): generate options for a stuck goal via the
 * ai-proxy. Convergence (TRI-45): the user filters to one or two, then turns a pick into a new goal,
 * a step on the stuck goal, or a logged Decision ("chance filtered by choice").
 */
@OptIn(ExperimentalUuidApi::class)
class PossibilityModeViewModel(
    private val goalId: String,
    private val goalRepository: GoalRepository,
    private val generatePossibilities: GeneratePossibilitiesUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val decisionRepository: DecisionRepository,
) : ViewModel() {

    private val _goal = MutableStateFlow<Goal?>(null)
    val goal: StateFlow<Goal?> = _goal.asStateFlow()

    private val _possibilities = MutableStateFlow<List<Possibility>>(emptyList())
    val possibilities: StateFlow<List<Possibility>> = _possibilities.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _isGenerating = MutableStateFlow(true)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Set to a short confirmation after a convergence action, so the screen can acknowledge it. */
    private val _actionDone = MutableStateFlow<String?>(null)
    val actionDone: StateFlow<String?> = _actionDone.asStateFlow()

    init {
        generate()
    }

    fun generate() {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            val g = runCatching { goalRepository.observeAllGoals().first().firstOrNull { it.id == goalId } }.getOrNull()
            _goal.value = g
            if (g == null) {
                _error.value = "That goal could not be found."
                _isGenerating.value = false
                return@launch
            }
            val result = generatePossibilities(g)
            _possibilities.value = result
            if (result.isEmpty()) _error.value = "Could not gather possibilities right now. Try again."
            _isGenerating.value = false
        }
    }

    fun toggleSelect(id: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply { if (!add(id)) remove(id) }
    }

    private fun selected(): List<Possibility> =
        _possibilities.value.filter { it.id in _selectedIds.value }

    /** Convergence: turn a possibility into its own new goal, carrying the parent's value + area. */
    fun makeGoal(p: Possibility) {
        val parent = _goal.value ?: return
        viewModelScope.launch {
            runCatching {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                createGoalUseCase(
                    Goal(
                        id = Uuid.random().toString(),
                        category = parent.category,
                        title = p.text.take(120),
                        description = "From Possibility Mode for \"${parent.title}\". ${p.rationale}",
                        status = GoalStatus.IN_PROGRESS,
                        timeline = parent.timeline,
                        dueDate = parent.dueDate,
                        createdAt = now,
                        valueId = parent.valueId,
                    )
                )
                _actionDone.value = "Added as a new goal"
            }.onFailure { Logger.w("PossibilityMode") { "makeGoal failed: ${it.message}" } }
        }
    }

    /** Convergence: attach a possibility as a concrete next step on the stuck goal. */
    fun addStep(p: Possibility) {
        viewModelScope.launch {
            runCatching {
                goalRepository.addMilestone(goalId, Milestone(id = Uuid.random().toString(), title = p.text.take(120)))
                _actionDone.value = "Added as a step"
            }.onFailure { Logger.w("PossibilityMode") { "addStep failed: ${it.message}" } }
        }
    }

    /** Convergence: record the deliberate re-choice as a Decision (Pillar 3), with the full option set. */
    fun logAsDecision() {
        val parent = _goal.value ?: return
        val picks = selected().ifEmpty { return }
        viewModelScope.launch {
            runCatching {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                decisionRepository.insertDecision(
                    Decision(
                        id = Uuid.random().toString(),
                        question = "How could I get unstuck on \"${parent.title}\"?",
                        optionsConsidered = _possibilities.value.map { it.text },
                        chosenOption = picks.joinToString("; ") { it.text },
                        reasoning = "Explored in Possibility Mode and chose to pursue this.",
                        relatedGoalId = goalId,
                        decidedAt = now,
                    )
                )
                _actionDone.value = "Logged as a decision"
            }.onFailure { Logger.w("PossibilityMode") { "logAsDecision failed: ${it.message}" } }
        }
    }

    fun clearActionDone() { _actionDone.value = null }
}
