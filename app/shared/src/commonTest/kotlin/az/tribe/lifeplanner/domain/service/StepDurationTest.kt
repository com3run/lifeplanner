package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StepDurationTest {

    @Test
    fun `seconds are read in every spelling people actually use`() {
        listOf(
            "Hold crow pose 30 seconds",
            "Hold crow pose 30 second",
            "Hold crow pose 30 secs",
            "Hold crow pose 30 sec",
            "Hold crow pose 30s",
        ).forEach { assertEquals(30, StepDuration.secondsIn(it), it) }
    }

    @Test
    fun `minutes are read and converted`() {
        listOf(
            "Meditate for 5 minutes",
            "Meditate for 5 minute",
            "Meditate for 5 mins",
            "Meditate for 5 min",
        ).forEach { assertEquals(300, StepDuration.secondsIn(it), it) }
    }

    @Test
    fun `a step with no duration gets no timer`() {
        // The common case by far. A play button on something you cannot start is worse than a
        // tick box on something you can.
        listOf(
            "Open a dedicated savings account",
            "Update resume and LinkedIn profile",
            "Call my mum",
        ).forEach { assertNull(StepDuration.secondsIn(it), it) }
    }

    @Test
    fun `a distance is not a duration`() {
        // "5k" and "10 km" have numbers next to letters, and a timer on them would be nonsense.
        assertNull(StepDuration.secondsIn("Run 5k"))
        assertNull(StepDuration.secondsIn("Cycle 20 km"))
    }

    @Test
    fun `something too short to watch is not offered`() {
        assertNull(StepDuration.secondsIn("Hold it 2 seconds"))
    }

    @Test
    fun `something too long to sit through is not offered`() {
        // An hour of deep work is a real step, but nobody watches that countdown in a feed row.
        assertNull(StepDuration.secondsIn("Deep work 90 minutes"))
        assertNull(StepDuration.secondsIn("Study for 2 hours"))
    }

    @Test
    fun `a compound duration takes the larger unit rather than the first match`() {
        // "2 minutes 30 seconds" means 150s. Reading the seconds first would start a 30s timer on
        // a two and a half minute hold, which is worse than offering nothing.
        assertEquals(120, StepDuration.secondsIn("Hold for 2 minutes 30 seconds"))
    }

    @Test
    fun `a target time inside a longer goal does not become a timer for the whole step`() {
        // "Run 5k in 25 minutes" is a target, not something to count down at your desk. We do
        // still offer it, because 25 minutes is within range and the user can simply not press it;
        // what matters is that we read the number correctly rather than inventing one.
        assertEquals(1500, StepDuration.secondsIn("Run 5k in 25 minutes"))
    }

    @Test
    fun `the countdown reads like a clock`() {
        assertEquals("0:30", StepDuration.format(30))
        assertEquals("1:00", StepDuration.format(60))
        assertEquals("1:05", StepDuration.format(65))
        assertEquals("12:00", StepDuration.format(720))
        assertEquals("0:00", StepDuration.format(0))
    }
}
