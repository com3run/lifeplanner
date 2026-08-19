package az.tribe.lifeplanner.domain.service

/**
 * The whole library, in the order one reader should meet it, so the Present tab can just keep
 * going.
 *
 * Learn used to be three cards and a link to a hub. You read the three, and the screen was done
 * with you. A page that ends is a page you leave, so the reading now runs off the bottom of the
 * present: the lesson you are on, then the rest of that path, then the next path, and when the
 * unread runs out, everything you have already read comes back around as a revisit rather than the
 * scroll hitting a wall.
 *
 * Ordering is decided once, here, and paging through it is the UI's business. That matters: if the
 * order were recomputed as lessons were marked read, finishing one would reshuffle the ground under
 * the reader's thumb.
 */
object LearningStream {

    data class Entry(
        val lesson: KnowledgeBit,
        val pathTitle: String,
        val pathEmoji: String,
        /** 1-based position of this lesson in its path, including locked ones, so it matches the hub. */
        val position: Int,
        val total: Int,
        /** Already read when the stream was built. Shown as a revisit rather than as new. */
        val revisit: Boolean,
    )

    /**
     * @param level gates the library: a lesson the user cannot open yet is not in the stream at all,
     *   because a wall of locked cards is a worse answer than a shorter list.
     */
    fun build(level: Int, readIds: Set<String>): List<Entry> {
        val collections = KnowledgeLibrary.collections
        if (collections.isEmpty()) return emptyList()

        // Start where the reader stopped, then walk the paths in order from there, wrapping. The
        // path in progress leads, so the first thing under the thumb is the thing they were doing.
        val resume = KnowledgeLibrary.resumePoint(level, readIds)
        val startIndex = collections.indexOfFirst { it.id == resume?.path?.id }.coerceAtLeast(0)
        val ordered = List(collections.size) { collections[(startIndex + it) % collections.size] }

        fun entriesOf(pathIndex: Int) = ordered[pathIndex].let { path ->
            val lessons = KnowledgeLibrary.lessonsOf(path)
            lessons.mapIndexedNotNull { i, lesson ->
                if (lesson.minLevel > level) return@mapIndexedNotNull null
                Entry(
                    lesson = lesson,
                    pathTitle = path.title,
                    pathEmoji = path.emoji,
                    position = i + 1,
                    total = lessons.size,
                    revisit = lesson.id in readIds,
                )
            }
        }

        val all = ordered.indices.flatMap(::entriesOf)
        // Unread first, in path order, then the read ones in the same order behind them. A reader
        // who has finished everything still gets a stream, it is just honestly labelled.
        val (unread, read) = all.partition { !it.revisit }
        return unread + read
    }
}
