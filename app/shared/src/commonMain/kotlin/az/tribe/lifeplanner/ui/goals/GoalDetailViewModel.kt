package az.tribe.lifeplanner.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * D7 — backs the redesigned **Goal Detail** screen. Reactive over the goal (so milestone toggles
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

    fun toggleMilestone(id: String, completed: Boolean) {
        viewModelScope.launch {
            runCatching { goalRepository.toggleMilestoneCompletion(id, completed) }
                .onFailure { Logger.w("GoalDetailViewModel") { "Toggle milestone failed: ${it.message}" } }
        }
    }
}
