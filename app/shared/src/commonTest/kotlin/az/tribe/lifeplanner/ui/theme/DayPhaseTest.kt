package az.tribe.lifeplanner.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The hour to [DayPhase] bands, including every boundary. The failure mode this guards is an
 * off-by-one at a band edge, which would be invisible in review and would only show up as a hero
 * gradient that looks wrong for an hour a day.
 */
class DayPhaseTest {

    @Test
    fun `every hour of the day maps to a phase`() {
        // NIGHT wraps midnight, so this also proves the else branch covers 22..23 and 0..4.
        (0..23).forEach { dayPhaseFor(it) }
    }

    @Test
    fun `band boundaries are exact`() {
        assertEquals(DayPhase.NIGHT, dayPhaseFor(4), "04:59 is still night")
        assertEquals(DayPhase.DAWN, dayPhaseFor(5), "dawn starts at 05:00")
        assertEquals(DayPhase.DAWN, dayPhaseFor(8), "08:59 is still dawn")
        assertEquals(DayPhase.DAY, dayPhaseFor(9), "day starts at 09:00")
        assertEquals(DayPhase.DAY, dayPhaseFor(16), "16:59 is still day")
        assertEquals(DayPhase.DUSK, dayPhaseFor(17), "dusk starts at 17:00")
        assertEquals(DayPhase.DUSK, dayPhaseFor(21), "21:59 is still dusk")
        assertEquals(DayPhase.NIGHT, dayPhaseFor(22), "night starts at 22:00")
    }

    @Test
    fun `night covers both sides of midnight`() {
        assertEquals(DayPhase.NIGHT, dayPhaseFor(23))
        assertEquals(DayPhase.NIGHT, dayPhaseFor(0))
        assertEquals(DayPhase.NIGHT, dayPhaseFor(3))
    }
}
