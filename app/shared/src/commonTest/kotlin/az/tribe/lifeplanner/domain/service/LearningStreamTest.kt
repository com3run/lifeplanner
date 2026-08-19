package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LearningStreamTest {

    private val everyLevel = 99

    @Test
    fun `the map opens on the path the reader is in the middle of`() {
        val motivation = KnowledgeLibrary.collections.first { it.id == "col_motivation" }
        val started = setOf(KnowledgeLibrary.lessonsOf(motivation).first().id)

        assertEquals(motivation.id, LearningStream.pathOrder(everyLevel, started).first().id)
    }

    @Test
    fun `an untouched library starts at the beginning`() {
        assertEquals(
            KnowledgeLibrary.collections.first().id,
            LearningStream.pathOrder(everyLevel, emptySet()).first().id,
        )
    }

    @Test
    fun `every path is walked once and the rest follow in library order`() {
        val mind = KnowledgeLibrary.collections.first { it.id == "col_mind" }
        val order = LearningStream.pathOrder(everyLevel, setOf(KnowledgeLibrary.lessonsOf(mind).first().id))

        assertEquals(KnowledgeLibrary.collections.size, order.size)
        assertEquals(order.map { it.id }.distinct().size, order.size)

        // After the path in progress, the map continues where the library does, wrapping around.
        val all = KnowledgeLibrary.collections.map { it.id }
        val startIndex = all.indexOf(mind.id)
        assertEquals(List(all.size) { all[(startIndex + it) % all.size] }, order.map { it.id })
    }

    @Test
    fun `a finished library still yields a map to walk again`() {
        val everything = KnowledgeLibrary.all.map { it.id }.toSet()
        assertTrue(LearningStream.pathOrder(everyLevel, everything).isNotEmpty())
    }
}
