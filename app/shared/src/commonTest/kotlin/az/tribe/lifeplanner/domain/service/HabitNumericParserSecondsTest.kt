package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals

class HabitNumericParserSecondsTest {

    @Test
    fun `parses seconds in the phrasings people actually type`() {
        assertEquals(30 to "sec", HabitNumericParser.parse("Plank for 30 seconds"))
        assertEquals(45 to "sec", HabitNumericParser.parse("Hold 45 sec"))
        assertEquals(20 to "sec", HabitNumericParser.parse("Cold shower 20s"))
    }

    @Test
    fun `does not mistake minutes or other units for seconds`() {
        assertEquals(10 to "min", HabitNumericParser.parse("Meditate 10 min"))
        assertEquals(2 to "hrs", HabitNumericParser.parse("Study 2 hours"))
        assertEquals(8 to "glasses", HabitNumericParser.parse("Drink 8 glasses"))
        assertEquals(5000 to "steps", HabitNumericParser.parse("Walk 5000 steps"))
    }
}
