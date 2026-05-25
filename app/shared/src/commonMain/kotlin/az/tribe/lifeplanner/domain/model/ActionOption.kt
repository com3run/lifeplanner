package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory

enum class ActionOptionType { HABIT, MILESTONE, FOCUS, GOAL }

/**
 * Pillar 2, an ephemeral (never persisted) action suggestion surfaced by the
 * [az.tribe.lifeplanner.domain.service.PossibilityEngine]: a goal / milestone /
 * habit / focus option plus a one-line, human-readable fit reason.
 */
data class ActionOption(
    val type: ActionOptionType,
    val refId: String,            // habit id, milestone id, or goal id
    val title: String,            // what to do
    val fitReason: String,        // one-line reason it fits right now
    val goalId: String? = null,   // parent goal for milestone/focus options
    val category: GoalCategory? = null,
    val score: Double = 0.0       // ranking score (higher = better)
)
