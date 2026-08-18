package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals

class DayPhaseTest {

    @Test
    fun `night runs from eight in the evening to six in the morning`() {
        assertEquals(DayPhase.NIGHT, DayPhase.of(20))
        assertEquals(DayPhase.NIGHT, DayPhase.of(23))
        assertEquals(DayPhase.NIGHT, DayPhase.of(0))
        assertEquals(DayPhase.NIGHT, DayPhase.of(5))
    }

    @Test
    fun `dawn is the first two hours of light`() {
        assertEquals(DayPhase.DAWN, DayPhase.of(6))
        assertEquals(DayPhase.DAWN, DayPhase.of(7))
    }

    @Test
    fun `day holds until five in the afternoon`() {
        assertEquals(DayPhase.DAY, DayPhase.of(8))
        assertEquals(DayPhase.DAY, DayPhase.of(12))
        assertEquals(DayPhase.DAY, DayPhase.of(16))
    }

    @Test
    fun `dusk is the last three hours before night`() {
        assertEquals(DayPhase.DUSK, DayPhase.of(17))
        assertEquals(DayPhase.DUSK, DayPhase.of(19))
    }

    @Test
    fun `hours outside the clock wrap instead of throwing`() {
        assertEquals(DayPhase.of(2), DayPhase.of(26))
        assertEquals(DayPhase.of(22), DayPhase.of(-2))
    }
}
