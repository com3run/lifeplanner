package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Pillar 3, the "good decision vs. good luck" distinction: process quality on one
 * axis, result on the other. Lets a review credit a sound decision that happened to
 * turn out badly (and flag a lucky bad decision).
 */
enum class OutcomeQuality {
    GOOD_PROCESS_GOOD_RESULT,
    GOOD_PROCESS_BAD_RESULT,
    BAD_PROCESS_GOOD_RESULT,
    BAD_PROCESS_BAD_RESULT
}

/**
 * Pillar 3, a deliberate choice recorded as a first-class object (not a mechanical
 * [GoalChange] diff): what was decided, the options weighed, the reasoning, and, later -
 * how it actually turned out. `null` outcome fields mean the decision hasn't been reviewed yet.
 */
data class Decision(
    val id: String,
    val question: String,
    val optionsConsidered: List<String> = emptyList(),
    val chosenOption: String,
    val reasoning: String = "",
    val relatedGoalId: String? = null,
    val expectedOutcome: String = "",
    val confidence: Int = 50,            // 0-100
    val decidedAt: LocalDateTime,
    val actualOutcome: String? = null,
    val outcomeReviewedAt: LocalDateTime? = null,
    val outcomeQuality: OutcomeQuality? = null
)
