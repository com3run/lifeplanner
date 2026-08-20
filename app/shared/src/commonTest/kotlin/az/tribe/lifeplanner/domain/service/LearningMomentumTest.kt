package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.BadgeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Reads against the real library rather than a fixture: the numbers this thing reports are the
 * user's actual position in the actual content, and a fixture would only prove the arithmetic
 * against itself. Expected counts are derived from the library for the same reason, so editing a
 * path does not fail a test that is not about content.
 */
class LearningMomentumTest {

    private val everyLevel = 99

    private val habits = KnowledgeLibrary.collections.first { it.id == "col_habits" }
    private val habitLessons = KnowledgeLibrary.lessonsOf(habits)

    private fun idsOfFirst(n: Int) = habitLessons.take(n).map { it.id }.toSet()

    @Test
    fun `an untouched path opens on the size of the step and not the size of the path`() {
        val s = assertNotNull(LearningMomentum.of(everyLevel, emptySet()))
        assertEquals(habits.title, s.pathTitle)
        assertEquals(LearningMomentum.Stage.OPENING, s.stage)
        assertEquals(1, s.position)
        assertEquals(0, s.read)
        assertEquals(habitLessons.size, s.total)
        assertEquals(habitLessons[0].title, s.lessonTitle)
        assertTrue(s.line.contains("${s.readMinutes} minutes"), "opening line should sell the step: ${s.line}")
    }

    @Test
    fun `the lesson after this one is named so there is something to come back for`() {
        val s = assertNotNull(LearningMomentum.of(everyLevel, emptySet()))
        assertEquals(habitLessons[1].title, s.upNextTitle)
        assertEquals(false, s.upNextStartsNewPath)
    }

    @Test
    fun `past the midpoint the line counts down instead of up`() {
        val half = habitLessons.size / 2
        val s = assertNotNull(LearningMomentum.of(everyLevel, idsOfFirst(half)))
        assertEquals(LearningMomentum.Stage.HALFWAY, s.stage)
        assertEquals(half + 1, s.position)
        assertTrue(s.line.startsWith("Past halfway"), s.line)
        assertTrue(s.line.contains("${s.readableAfterThis + 1} to go"), s.line)
    }

    @Test
    fun `close to the end the badge is named`() {
        val s = assertNotNull(LearningMomentum.of(everyLevel, idsOfFirst(habitLessons.size - 2)))
        assertEquals(LearningMomentum.Stage.CLOSING, s.stage)
        assertEquals(BadgeType.LEARN_HABITS, s.badge)
        assertTrue(s.line.contains(BadgeType.LEARN_HABITS.displayName), s.line)
    }

    @Test
    fun `the last lesson says it is the last one`() {
        val s = assertNotNull(LearningMomentum.of(everyLevel, idsOfFirst(habitLessons.size - 1)))
        assertEquals(LearningMomentum.Stage.LAST, s.stage)
        assertEquals(0, s.readableAfterThis)
        assertTrue(s.line.startsWith("Last one."), s.line)
    }

    @Test
    fun `a badge out of reach is not promised`() {
        // At level 1 part of the path is still locked, so clearing it today is impossible.
        val unlockedIds = habitLessons.filter { it.minLevel <= 1 }.map { it.id }
        assertTrue(unlockedIds.size < habitLessons.size, "test needs a path with locked lessons at level 1")

        val allButLast = unlockedIds.dropLast(1).toSet()
        val s = assertNotNull(LearningMomentum.of(1, allButLast))
        assertEquals(habits.title, s.pathTitle)
        assertNull(s.badge)
        assertTrue(s.lockedAhead > 0)
        assertTrue(s.line.contains("unlocks"), s.line)
    }

    @Test
    fun `finishing a path opens the next one`() {
        val allHabits = habitLessons.map { it.id }.toSet()
        val s = assertNotNull(LearningMomentum.of(everyLevel, allHabits))
        assertTrue(s.pathTitle != habits.title, "should have moved on from ${habits.title}")
        assertEquals(LearningMomentum.Stage.OPENING, s.stage)
    }

    @Test
    fun `with nothing left to read it says nothing`() {
        val everything = KnowledgeLibrary.all.map { it.id }.toSet()
        assertNull(LearningMomentum.of(everyLevel, everything))
    }

    @Test
    fun `the reward is stated before the lesson is read`() {
        val s = assertNotNull(LearningMomentum.of(everyLevel, emptySet()))
        assertEquals(az.tribe.lifeplanner.domain.model.XpRewards.LESSON_READ, s.xp)
    }
}
