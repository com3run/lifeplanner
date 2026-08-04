package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BreathMomentTest {

    @Test
    fun `an ordinary day says nothing`() {
        // The default has to be silence. This is the whole fix: a prompt that appears every day
        // stops being a reminder and becomes something the user is trained to scroll past.
        assertNull(BreathMoment.of(hourOfDay = 14, planItems = 2, mentalScore = 7.0, breathsToday = 0))
    }

    @Test
    fun `a low Mental score is the strongest reason and names itself`() {
        val moment = BreathMoment.of(hourOfDay = 14, planItems = 1, mentalScore = 3.5, breathsToday = 0)

        assertNotNull(moment)
        // The user told us this themselves, so saying it back is honest rather than presumptuous.
        assertTrue("Mental" in moment.reason, moment.reason)
    }

    @Test
    fun `a heavy morning is worth a word`() {
        val moment = BreathMoment.of(hourOfDay = 8, planItems = 6, mentalScore = 8.0, breathsToday = 0)

        assertNotNull(moment)
        assertTrue("6 things" in moment.reason, moment.reason)
    }

    @Test
    fun `a heavy day already underway is not interrupted`() {
        // At 4pm, being told the day is busy is not news, and it is too late to act on.
        assertNull(BreathMoment.of(hourOfDay = 16, planItems = 6, mentalScore = 8.0, breathsToday = 0))
    }

    @Test
    fun `things slipping is a reason, and the copy does not scold`() {
        val moment = BreathMoment.of(hourOfDay = 15, planItems = 3, mentalScore = 8.0, breathsToday = 0, overdueItems = 4)

        assertNotNull(moment)
        // Someone behind on their week does not need to be told off by a breathing prompt.
        assertTrue("longer" in moment.reason, moment.reason)
        assertTrue("!" !in moment.reason, moment.reason)
    }

    @Test
    fun `late evening is offered`() {
        assertNotNull(BreathMoment.of(hourOfDay = 22, planItems = 0, mentalScore = 8.0, breathsToday = 0))
    }

    @Test
    fun `having already breathed today ends it, whatever else is true`() {
        // Every other signal at once, and it still stays quiet, because the user already did it.
        assertNull(
            BreathMoment.of(
                hourOfDay = 22,
                planItems = 9,
                mentalScore = 2.0,
                breathsToday = 1,
                overdueItems = 5,
            )
        )
    }

    @Test
    fun `an unset Mental score is not treated as a low one`() {
        // Null means the user has not told us. Acting as though silence meant "bad" is how an app
        // starts inventing a mood for someone.
        assertNull(BreathMoment.of(hourOfDay = 14, planItems = 1, mentalScore = null, breathsToday = 0))
    }

    @Test
    fun `every reason is a sentence worth reading`() {
        val moments = listOf(
            BreathMoment.of(14, 1, 3.0, 0),
            BreathMoment.of(8, 7, 8.0, 0),
            BreathMoment.of(15, 3, 8.0, 0, overdueItems = 4),
            BreathMoment.of(22, 0, 8.0, 0),
        )

        moments.forEach { m ->
            assertNotNull(m)
            assertTrue(m.reason.length > 25, "reason too thin to justify appearing: ${m.reason}")
            assertTrue(m.reason.trim().endsWith("."), "unfinished: ${m.reason}")
        }
    }
}
