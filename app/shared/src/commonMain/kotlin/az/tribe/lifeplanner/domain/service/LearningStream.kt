package az.tribe.lifeplanner.domain.service

/**
 * The order this reader should walk the map in.
 *
 * The hub lists the zones in library order, which is right for a page you went looking for. The
 * Present tab is not that page: it opens on whatever you were last doing, so the trail you are
 * halfway along has to be the one under your thumb, with the rest of the map running on behind it.
 *
 * Decided once and held, deliberately. If this were recomputed as lessons were read, clearing a
 * zone would slide the whole map while the reader was looking at it.
 */
object LearningStream {

    /**
     * Paths in walking order: the one in progress first, then the rest in library order, wrapping.
     * Empty only when the library itself is.
     */
    fun pathOrder(level: Int, readIds: Set<String>): List<KnowledgeCollection> {
        val collections = KnowledgeLibrary.collections
        if (collections.isEmpty()) return emptyList()
        val resume = KnowledgeLibrary.resumePoint(level, readIds)
        val start = collections.indexOfFirst { it.id == resume?.path?.id }.coerceAtLeast(0)
        return List(collections.size) { collections[(start + it) % collections.size] }
    }
}
