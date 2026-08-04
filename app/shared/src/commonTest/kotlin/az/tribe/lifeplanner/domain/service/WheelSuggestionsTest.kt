package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.WheelArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WheelSuggestionsTest {

    @Test
    fun `Friends has copy at every urgency`() {
        NudgeUrgency.entries.forEach { urgency ->
            assertNotNull(
                WheelSuggestions.forArea(WheelArea.FRIENDS, urgency),
                "Friends has nothing to say at $urgency",
            )
        }
    }

    @Test
    fun `an area with no copy yet stays silent rather than inventing something`() {
        (WheelArea.entries - WheelSuggestions.covered).forEach { area ->
            NudgeUrgency.entries.forEach { urgency ->
                assertNull(
                    WheelSuggestions.forArea(area, urgency),
                    "$area returned copy that was never written for it",
                )
            }
        }
    }

    @Test
    fun `rotation cycles rather than repeating or running off the end`() {
        val seen = (0..5).map { WheelSuggestions.forArea(WheelArea.FRIENDS, NudgeUrgency.GENTLE, it)?.action }

        assertEquals(3, seen.take(3).toSet().size, "three consecutive days gave the same advice")
        // Day 3 comes back around to day 0 rather than throwing or falling off the list.
        assertEquals(seen[0], seen[3])
    }

    @Test
    fun `a negative rotation is handled, since callers pass date arithmetic`() {
        val suggestion = WheelSuggestions.forArea(WheelArea.FRIENDS, NudgeUrgency.GENTLE, -1)

        assertNotNull(suggestion)
    }

    @Test
    fun `every suggestion is an action and a reason, not a slogan`() {
        NudgeUrgency.entries.forEach { urgency ->
            (0..2).forEach { rotation ->
                val s = WheelSuggestions.forArea(WheelArea.FRIENDS, urgency, rotation) ?: return@forEach
                assertTrue(s.action.isNotBlank(), "$urgency/$rotation has no action")
                assertTrue(s.because.isNotBlank(), "$urgency/$rotation has no reason")
                // Long enough to be specific. "Reach out" is the failure mode this guards against.
                assertTrue(s.action.length > 24, "action too vague to act on: ${s.action}")
                assertEquals(WheelArea.FRIENDS, s.area)
                assertEquals(urgency, s.urgency)
            }
        }
    }

    @Test
    fun `the serious copy does not ask for more than one small thing`() {
        val serious = (0..2).mapNotNull {
            WheelSuggestions.forArea(WheelArea.FRIENDS, NudgeUrgency.SERIOUS, it)
        }

        // Someone at a 3 is being asked for the smallest possible step, so nothing here should
        // read as a project. Multiple sentences of instruction is the tell.
        serious.forEach {
            assertTrue(
                it.action.count { c -> c == '.' } <= 2,
                "serious advice should be one step, was: ${it.action}",
            )
        }
    }
}
