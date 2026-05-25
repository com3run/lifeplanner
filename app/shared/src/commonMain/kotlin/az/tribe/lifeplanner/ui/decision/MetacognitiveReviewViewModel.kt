package az.tribe.lifeplanner.ui.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.OutcomeQuality
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Pillar 5 (P5.4), metacognitive review: surfaces logged Decisions for the user to grade
 * their *reasoning* (process), separate from how it turned out (result). Sets [Decision.outcomeQuality].
 */
class MetacognitiveReviewViewModel(
    private val decisionRepository: DecisionRepository,
) : ViewModel() {

    // Un-graded decisions first, then most recent.
    val decisions: StateFlow<List<Decision>> =
        decisionRepository.observeAllDecisions()
            .map { list ->
                list.sortedWith(
                    compareByDescending<Decision> { it.outcomeQuality == null }.thenByDescending { it.decidedAt }
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun grade(decision: Decision, quality: OutcomeQuality) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            decisionRepository.updateDecision(
                decision.copy(outcomeQuality = quality, outcomeReviewedAt = now)
            )
        }
    }
}
