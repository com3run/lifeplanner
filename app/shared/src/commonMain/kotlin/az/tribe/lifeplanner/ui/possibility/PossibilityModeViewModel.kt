package az.tribe.lifeplanner.ui.possibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.model.Possibility
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.service.LocalPossibilityFallback
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

    /** True while the AI is widening the options in the background. Local options already show. */
    private val _isEnhancing = MutableStateFlow(true)
    val isEnhancing: StateFlow<Boolean> = _isEnhancing.asStateFlow()

    private val localFallback = LocalPossibilityFallback()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * A one-shot navigation request emitted after a convergence action, so the screen moves the user
     * somewhere real instead of just flashing a toast. Cleared once handled.
     */
    private val _nav = MutableStateFlow<PossibilityNav?>(null)
    val nav: StateFlow<PossibilityNav?> = _nav.asStateFlow()
    fun consumeNav() { _nav.value = null }

    init {
        generate()
    }

    fun generate() {
        viewModelScope.launch {
            _isEnhancing.value = true
            _error.value = null
            val g = runCatching { goalRepository.observeAllGoals().first().firstOrNull { it.id == goalId } }.getOrNull()
            _goal.value = g
            if (g == null) {
                _error.value = "That goal could not be found."
                _isEnhancing.value = false
                return@launch
            }
            // Instant: show local options right away so there is never a blank wait.
            if (_possibilities.value.isEmpty()) _possibilities.value = localFallback(g)
            // Enhance: swap in the AI's wider set when it arrives; keep the local ones if it fails.
            val ai = generatePossibilities(g)
            if (ai.any { !it.isLocal }) {
                _possibilities.value = ai
                _selectedIds.value = emptySet() // the option set changed under the user
            }
            if (_possibilities.value.isEmpty()) _error.value = "Could not gather possibilities right now. Try again."
            _isEnhancing.value = false
        }
    }

    fun toggleSelect(id: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply { if (!add(id)) remove(id) }
    }

    private fun selected(): List<Possibility> =
        _possibilities.value.filter { it.id in _selectedIds.value }

    /** Convergence: turn the selected possibilities into new goals, then open the one created (or go back). */
    fun makeGoalsFromSelection() {
        val parent = _goal.value ?: return
        val picks = selected().ifEmpty { return }
        viewModelScope.launch {
            val createdIds = mutableListOf<String>()
            picks.forEach { p ->
                runCatching {
                    val id = Uuid.random().toString()
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    createGoalUseCase(
                        Goal(
                            id = id,
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
                    createdIds += id
                }.onFailure { Logger.w("PossibilityMode") { "makeGoal failed: ${it.message}" } }
            }
            _nav.value = createdIds.singleOrNull()?.let { PossibilityNav.OpenGoal(it) } ?: PossibilityNav.Back
        }
    }

    /** Convergence: attach the selected possibilities as one-time steps on the stuck goal, then open it. */
    fun addStepsFromSelection() {
        val picks = selected().ifEmpty { return }
        viewModelScope.launch {
            runCatching {
                picks.forEach { p ->
                    goalRepository.addMilestone(goalId, Milestone(id = Uuid.random().toString(), title = p.text.take(120)))
                }
            }.onFailure { Logger.w("PossibilityMode") { "addStep failed: ${it.message}" } }
            // Return to the goal so the new steps are visible, instead of a silent toast.
            _nav.value = PossibilityNav.OpenGoal(goalId)
        }
    }

    /** Convergence: record the deliberate re-choice as a Decision (Pillar 3), then open it. */
    fun logSelectionAsDecision() {
        val parent = _goal.value ?: return
        val picks = selected().ifEmpty { return }
        viewModelScope.launch {
            val id = Uuid.random().toString()
            runCatching {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                decisionRepository.insertDecision(
                    Decision(
                        id = id,
                        question = "How could I get unstuck on \"${parent.title}\"?",
                        optionsConsidered = _possibilities.value.map { it.text },
                        chosenOption = picks.joinToString("; ") { it.text },
                        reasoning = "Explored in Possibility Mode and chose to pursue this.",
                        relatedGoalId = goalId,
                        decidedAt = now,
                    )
                )
                _nav.value = PossibilityNav.OpenDecision(id)
            }.onFailure { Logger.w("PossibilityMode") { "logAsDecision failed: ${it.message}" } }
        }
    }

    /**
     * Hand the situation to the user's coach. Builds a context-rich opener from the stuck goal and any
     * picks, so when chat opens the persona reacts to *this* situation, and invites the user in.
     */
    fun talkToCoach() {
        val parent = _goal.value ?: return
        val picks = selected()
        val message = buildString {
            append("I feel stuck on my goal \"${parent.title}\". ")
            if (picks.isNotEmpty()) {
                append("I explored some options in Possibility Mode and I'm considering: ")
                append(picks.joinToString("; ") { it.text })
                append(". ")
            }
            append(
                "Please suggest one small, concrete first step I could take today, and a sentence on why " +
                    "it helps. Keep it specific, not abstract. Then, only if you need to tailor it, ask me one question."
            )
        }
        _nav.value = PossibilityNav.TalkToCoach(coachFor(parent.category), message)
    }

    /** The built-in coach whose expertise best fits the stuck goal's area. */
    private fun coachFor(category: GoalCategory): String = when (category) {
        GoalCategory.CAREER -> "alex_career"
        GoalCategory.MONEY -> "morgan_finance"
        GoalCategory.BODY -> "kai_fitness"
        GoalCategory.PEOPLE -> "sam_social"
        GoalCategory.WELLBEING -> "river_wellness"
        GoalCategory.FAMILY -> "jamie_family"
        GoalCategory.PURPOSE -> "luna_general"
    }
}

/** A one-shot destination after a Possibility Mode action. */
sealed interface PossibilityNav {
    data class OpenGoal(val goalId: String) : PossibilityNav
    data class OpenDecision(val decisionId: String) : PossibilityNav
    data class TalkToCoach(val coachId: String, val message: String) : PossibilityNav
    data object Back : PossibilityNav
}
