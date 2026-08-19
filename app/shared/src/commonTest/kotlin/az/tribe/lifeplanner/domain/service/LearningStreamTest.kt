package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LearningStreamTest {

    private val everyLevel = 99

    @Test
    fun `the stream opens on the path the reader is in the middle of`() {
        val motivation = KnowledgeLibrary.collections.first { it.id == "col_motivation" }
        val firstOfMotivation = KnowledgeLibrary.lessonsOf(motivation).first()

        val stream = LearningStream.build(everyLevel, setOf(firstOfMotivation.id))
        assertEquals(motivation.title, stream.first().pathTitle)
    }

    @Test
    fun `unread lessons come before ones already read`() {
        val first = KnowledgeLibrary.collections.first()
        val read = KnowledgeLibrary.lessonsOf(first).take(2).map { it.id }.toSet()

        val stream = LearningStream.build(everyLevel, read)
        val firstRevisit = stream.indexOfFirst { it.revisit }
        val lastUnread = stream.indexOfLast { !it.revisit }
        assertTrue(firstRevisit > lastUnread, "read lessons should sit behind every unread one")
        assertEquals(read.size, stream.count { it.revisit })
    }

    @Test
    fun `a reader who has finished everything still gets a stream`() {
        val everything = KnowledgeLibrary.all.map { it.id }.toSet()
        val stream = LearningStream.build(everyLevel, everything)
        assertTrue(stream.isNotEmpty())
        assertTrue(stream.all { it.revisit })
    }

    @Test
    fun `locked lessons are left out rather than shown as a wall`() {
        val stream = LearningStream.build(1, emptySet())
        assertTrue(stream.isNotEmpty())
        assertFalse(stream.any { it.lesson.minLevel > 1 })
        assertTrue(stream.size < KnowledgeLibrary.all.size, "level 1 should not see the whole library")
    }

    @Test
    fun `every lesson is offered once and carries its place in its path`() {
        val stream = LearningStream.build(everyLevel, emptySet())
        assertEquals(stream.map { it.lesson.id }.distinct().size, stream.size)
        stream.forEach { e ->
            assertTrue(e.position in 1..e.total, "${e.lesson.id}: ${e.position} of ${e.total}")
        }
    }
}
