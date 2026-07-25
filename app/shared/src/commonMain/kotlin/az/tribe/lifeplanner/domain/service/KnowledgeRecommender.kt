package az.tribe.lifeplanner.domain.service

/**
 * A snapshot of what the user is currently doing, distilled into a weight per [KnowledgeTopic].
 * Higher weight means the user is more active in that area, so lessons on it are more relevant.
 * The mapping from raw activity (habits, goals, patterns) to weights lives in the caller so this
 * stays pure and testable.
 */
data class KnowledgeSignals(
    val level: Int,
    val readIds: Set<String>,
    val topicAffinity: Map<KnowledgeTopic, Double> = emptyMap(),
    val daySeed: Int = 0,
)

/**
 * Decides which lessons to surface. Pure and deterministic given its inputs. Ranks unlocked lessons
 * by how well they match the user's activity ([KnowledgeSignals.topicAffinity]), always preferring
 * unread ones, with a gentle day-based tiebreak so the feed rotates instead of showing the same
 * lesson forever. Falls back gracefully to level + rotation when there is no activity signal yet.
 */
object KnowledgeRecommender {

    /** Every unlocked lesson, ranked best-first. Read lessons sink to the bottom, they are not removed. */
    fun rank(
        signals: KnowledgeSignals,
        library: List<KnowledgeBit> = KnowledgeLibrary.all,
    ): List<KnowledgeBit> =
        library.filter { it.minLevel <= signals.level }
            .sortedByDescending { score(it, signals) }

    /** Top [count] unlocked, UNREAD lessons for "recommended for you". */
    fun recommend(
        signals: KnowledgeSignals,
        count: Int,
        library: List<KnowledgeBit> = KnowledgeLibrary.all,
    ): List<KnowledgeBit> =
        rank(signals, library).filterNot { it.id in signals.readIds }.take(count)

    private fun score(bit: KnowledgeBit, signals: KnowledgeSignals): Double {
        val relevance = bit.topics.sumOf { signals.topicAffinity[it] ?: 0.0 }
        val unreadBonus = if (bit.id in signals.readIds) 0.0 else UNREAD_BONUS
        // Deterministic per-day jitter so equally-relevant lessons rotate day to day.
        val jitter = (((bit.id.hashCode() xor signals.daySeed) and 0xFFFF) / 65535.0) * FRESHNESS
        return relevance + unreadBonus + jitter
    }

    private const val UNREAD_BONUS = 10.0
    private const val FRESHNESS = 1.0
}
