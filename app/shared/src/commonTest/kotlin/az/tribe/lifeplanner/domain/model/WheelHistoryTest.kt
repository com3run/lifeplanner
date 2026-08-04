package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WheelHistoryTest {

    private val monday = LocalDate(2026, 8, 3)
    private val weekBefore = LocalDate(2026, 7, 27)

    private fun snapshot(date: LocalDate, vararg scores: Pair<WheelArea, Double>) =
        WheelSnapshot(date, scores.toMap())

    @Test
    fun `what rose and what fell are reported separately, biggest first`() {
        val before = snapshot(
            weekBefore,
            WheelArea.PHYSICAL to 4.0,
            WheelArea.MONEY to 8.0,
            WheelArea.MENTAL to 6.0,
        )
        val now = snapshot(
            monday,
            WheelArea.PHYSICAL to 7.0,
            WheelArea.MONEY to 5.0,
            WheelArea.MENTAL to 6.5,
        )

        val comparison = compareWheels(ComparisonPeriod.WEEK, before, now)

        assertEquals(listOf(WheelArea.PHYSICAL, WheelArea.MENTAL), comparison.risen.map { it.area })
        assertEquals(listOf(WheelArea.MONEY), comparison.fallen.map { it.area })
        assertEquals(3.0, comparison.risen.first().change)
        assertEquals(-3.0, comparison.fallen.first().change)
    }

    @Test
    fun `the headline is the largest move in either direction`() {
        val before = snapshot(weekBefore, WheelArea.PHYSICAL to 5.0, WheelArea.MONEY to 9.0)
        val now = snapshot(monday, WheelArea.PHYSICAL to 6.0, WheelArea.MONEY to 4.0)

        val comparison = compareWheels(ComparisonPeriod.WEEK, before, now)

        // A five point drop matters more than a one point gain, even though the gain is the good news.
        assertEquals(WheelArea.MONEY, comparison.headline?.area)
        assertEquals(-5.0, comparison.headline?.change)
    }

    @Test
    fun `a wheel that did not move has no headline`() {
        val scores = arrayOf(WheelArea.PHYSICAL to 6.0, WheelArea.MONEY to 7.0)
        val comparison = compareWheels(
            ComparisonPeriod.WEEK,
            snapshot(weekBefore, *scores),
            snapshot(monday, *scores),
        )

        assertNull(comparison.headline)
        assertTrue(!comparison.hasMovement)
        assertEquals(2, comparison.unchanged.size)
        assertEquals(0.0, comparison.overallChange)
    }

    @Test
    fun `an area scored for the first time is named, not counted as a gain`() {
        val before = snapshot(weekBefore, WheelArea.PHYSICAL to 6.0)
        val now = snapshot(
            monday,
            WheelArea.PHYSICAL to 6.0,
            WheelArea.ROMANCE to 8.0,
            WheelArea.MISSION to 7.0,
        )

        val comparison = compareWheels(ComparisonPeriod.WEEK, before, now)

        // Not gains: scoring an area for the first time is not the same as improving it.
        assertTrue(comparison.risen.isEmpty())
        assertEquals(0.0, comparison.overallChange)
        // But not silent either. Without this the card reports "nothing moved" at someone who
        // just filled in two areas, and reads as broken rather than careful.
        assertEquals(listOf(WheelArea.MISSION, WheelArea.ROMANCE), comparison.newlyScored)
        assertTrue(comparison.hasSomethingToSay)
        assertTrue(!comparison.hasMovement)
    }

    @Test
    fun `only areas that actually moved are offered for drawing over the wheel`() {
        val before = snapshot(weekBefore, WheelArea.PHYSICAL to 4.0, WheelArea.MONEY to 7.0)
        val now = snapshot(
            monday,
            WheelArea.PHYSICAL to 7.0,
            WheelArea.MONEY to 7.0,
            WheelArea.ROMANCE to 8.0,
        )

        val moved = compareWheels(ComparisonPeriod.WEEK, before, now).movedFrom

        // Money did not move, so a ghost of it would be noise sitting exactly under the fill.
        // Romance has no previous value at all, so there is no ghost to draw.
        assertEquals(mapOf(WheelArea.PHYSICAL to 4.0), moved)
    }

    @Test
    fun `an area missing from either side is skipped, not treated as a fall to zero`() {
        val before = snapshot(weekBefore, WheelArea.PHYSICAL to 6.0)
        val now = snapshot(monday, WheelArea.PHYSICAL to 7.0, WheelArea.ROMANCE to 8.0)

        val comparison = compareWheels(ComparisonPeriod.WEEK, before, now)

        // Romance had no reading a week ago. Reporting it as a jump from nothing would invent a
        // trend out of the user simply having set a score for the first time.
        assertEquals(listOf(WheelArea.PHYSICAL), comparison.deltas.map { it.area })
    }

    @Test
    fun `overall change averages the segments and ignores Joy`() {
        val before = snapshot(
            weekBefore,
            WheelArea.PHYSICAL to 4.0,
            WheelArea.MONEY to 4.0,
            WheelArea.JOY to 1.0,
        )
        val now = snapshot(
            monday,
            WheelArea.PHYSICAL to 6.0,
            WheelArea.MONEY to 6.0,
            WheelArea.JOY to 10.0,
        )

        val comparison = compareWheels(ComparisonPeriod.WEEK, before, now)

        // Both segments rose 2, so the wheel rose 2. Joy's nine point swing is reported but must
        // not drag the wheel's own number with it.
        assertEquals(2.0, comparison.overallChange)
    }

    @Test
    fun `the date actually compared against is reported, not the date asked for`() {
        // The user did not open the app a week ago, so the nearest earlier snapshot is used. Saying
        // "this week" while silently measuring from eleven days back would misrepresent the span.
        val actual = LocalDate(2026, 7, 23)
        val comparison = compareWheels(
            ComparisonPeriod.WEEK,
            snapshot(actual, WheelArea.PHYSICAL to 5.0),
            snapshot(monday, WheelArea.PHYSICAL to 6.0),
        )

        assertEquals(actual, comparison.previousDate)
        assertEquals(monday, comparison.currentDate)
    }

    @Test
    fun `a snapshot's overall matches the wheel's own averaging`() {
        val snap = snapshot(
            monday,
            WheelArea.PHYSICAL to 6.0,
            WheelArea.MONEY to 7.0,
            WheelArea.JOY to 10.0,
        )

        assertEquals(6.5, snap.overall)
    }

    @Test
    fun `every period has a span and a name to show`() {
        ComparisonPeriod.entries.forEach {
            assertTrue(it.days > 0, "${it.name} has no span")
            assertTrue(it.displayName.isNotBlank(), "${it.name} has no label")
        }
    }
}
