package az.tribe.lifeplanner.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.mapper.createNewMilestone
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.service.GoalValueInferrer
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * D7, backs the redesigned **Goal Detail** screen. Reactive over the goal (so milestone toggles
 * reflect immediately) and resolves the goal's **value** (Pillar 1 Why-Chain) from `valueId`.
 */
class GoalDetailViewModel(
    private val goalId: String,
    private val goalRepository: GoalRepository,
    private val lifeValueRepository: LifeValueRepository,
) : ViewModel() {

    val goal: StateFlow<Goal?> =
        goalRepository.observeAllGoals()
            .map { all -> all.firstOrNull { it.id == goalId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The life value this goal serves (its "why"), resolved from [Goal.valueId]; null if unlinked. */
    val valueTitle: StateFlow<String?> =
        goal.map { g -> g?.valueId?.let { runCatching { lifeValueRepository.getLifeValueById(it)?.title }.getOrNull() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * For an unlinked goal, the best-fitting value we can infer, so the detail can offer a one-tap
     * "this is my why" instead of nagging the user to go hunt through Edit. Null when linked already
     * or when nothing fits (then the screen falls back to the category/coach framing).
     */
    val suggestedValue: StateFlow<LifeValue?> =
        combine(goal, lifeValueRepository.observeAllLifeValues()) { g, values ->
            if (g == null || g.valueId != null) null
            else GoalValueInferrer.infer(g.category, g.title, g.description, values)
                ?.let { id -> values.firstOrNull { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** One-tap accept of a suggested (or chosen) value as this goal's why. */
    fun linkValue(valueId: String) {
        viewModelScope.launch {
            val g = goal.value ?: return@launch
            runCatching { goalRepository.updateGoal(g.copy(valueId = valueId)) }
                .onFailure { Logger.w("GoalDetailViewModel") { "Link value failed: ${it.message}" } }
        }
    }

    /**
     * Adds a step to the plan, whether it came from the coach's draft, a reworded version of it, or
     * the user's own line. Blank titles and exact repeats are dropped rather than surfaced as errors,
     * since the card is a fast tap-tap-tap surface and a double tap should be a no-op.
     */
    fun addMilestone(title: String) {
        val text = title.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            val g = goal.value ?: return@launch
            if (g.milestones.any { it.title.equals(text, ignoreCase = true) }) return@launch
            runCatching { goalRepository.addMilestone(goalId, createNewMilestone(text)) }
                .onFailure { Logger.w("GoalDetailViewModel") { "Add milestone failed: ${it.message}" } }
        }
    }

    fun toggleMilestone(id: String, completed: Boolean) {
        viewModelScope.launch {
            runCatching { goalRepository.toggleMilestoneCompletion(id, completed) }
                .onFailure { Logger.w("GoalDetailViewModel") { "Toggle milestone failed: ${it.message}" } }
        }
    }
}
