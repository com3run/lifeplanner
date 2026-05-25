package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory

/**
 * One card in the "For You" home feed. A flat shape on purpose: the builder assembles a ranked list
 * of these from many sources (knowledge library, causal insights, becoming, possibilities, momentum)
 * and the UI renders each by [kind]. Keeps the feed easy to consume, the app does the thinking.
 */
data class FeedItem(
    val id: String,
    val kind: FeedKind,
    val eyebrow: String,
    val title: String,
    val body: String,
    val emoji: String? = null,
    /** Drives the accent/gradient for action cards. Null falls back to the kind's default accent. */
    val category: GoalCategory? = null,
    /** When set, the card shows a primary button with this label. */
    val actionLabel: String? = null,
    /** When set, tapping the action checks in this habit for today (inline, no navigation). */
    val actionHabitId: String? = null,
    /** When set, tapping the card body navigates to this route (deep link into the app). */
    val route: String? = null,
    /** Higher sorts earlier within its interleave slot. */
    val score: Double = 0.0,
)

/** The visual + semantic family of a feed card. The UI maps each to an icon, accent, and layout. */
enum class FeedKind {
    /** A statistical finding about the user's own behavior (CausalInsightEngine). */
    INSIGHT,

    /** A curated, science-backed micro-lesson from the knowledge library. */
    KNOWLEDGE,

    /** The single best next action right now (PossibilityEngine), often with an inline check-in. */
    DO_NEXT,

    /** Progress on who the user is becoming (identity statements + value alignment, Pillar 1). */
    BECOMING,

    /** A momentum moment: streak milestone, level progress, weekly recap. */
    MOMENTUM,

    /** A timing/usage pattern observed from how the user actually uses the app. */
    PATTERN,
}
