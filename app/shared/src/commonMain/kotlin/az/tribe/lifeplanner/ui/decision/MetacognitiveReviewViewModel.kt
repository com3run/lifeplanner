package az.tribe.lifeplanner.ui.decision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.DecisionStatus
import az.tribe.lifeplanner.domain.model.OutcomeQuality
import az.tribe.lifeplanner.domain.model.XpAward
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.usecases.ability.AwardDecisionXpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
    private val awardDecisionXp: AwardDecisionXpUseCase,
) : ViewModel() {

    private val _lastAward = MutableStateFlow<XpAward?>(null)

    /** Set when a review just earned XP, so the screen can show the burst. Cleared by [clearAward]. */
    val lastAward: StateFlow<XpAward?> = _lastAward.asStateFlow()

    fun clearAward() { _lastAward.value = null }

    // Only confirmed decisions are reviewable, un-graded first, then most recent.
    val decisions: StateFlow<List<Decision>> =
        decisionRepository.observeAllDecisions()
            .map { list ->
                list.filter { it.status == DecisionStatus.CONFIRMED }
                    .sortedWith(
                        compareByDescending<Decision> { it.outcomeQuality == null }.thenByDescending { it.decidedAt }
                    )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun grade(decision: Decision, quality: OutcomeQuality) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            // Only a first review pays. Changing your mind about an already-graded decision is
            // fine and should stay free of charge, otherwise re-grading is an XP tap.
            val isFirstReview = decision.outcomeQuality == null
            decisionRepository.updateDecision(
                decision.copy(outcomeQuality = quality, outcomeReviewedAt = now)
            )
            if (isFirstReview) {
                _lastAward.value = awardDecisionXp.onDecisionReviewed(quality)
            }
        }
    }
}
