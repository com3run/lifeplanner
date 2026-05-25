package az.tribe.lifeplanner.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.repository.GoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * D7, backs the redesigned **Goals** canvas (D2: "what am I working toward, and why?"). Splits the
 * user's goals into active (work in progress) and completed, sorted by nearest due date.
 *
 * The "why" tag (value laddering / Why-Chain) is a **Pillar 1 seam**: `Goal.valueId` doesn't exist
 * on `main` yet, so today the visible tag is the goal's (now canonical) category; when Pillar 1
 * lands, the card grows a value tag + a one-tap Why-Chain.
 */
class GoalsViewModel(
    goalRepository: GoalRepository,
) : ViewModel() {

    val state: StateFlow<GoalsState> =
        goalRepository.observeAllGoals()
            .map { all ->
                val visible = all.filterNot { it.isArchived }.sortedBy { it.dueDate }
                GoalsState(
                    active = visible.filter { it.status != GoalStatus.COMPLETED },
                    completed = visible.filter { it.status == GoalStatus.COMPLETED },
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsState())
}

data class GoalsState(
    val active: List<Goal> = emptyList(),
    val completed: List<Goal> = emptyList(),
)
