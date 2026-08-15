package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.WheelScore
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WheelNudgePickerTest {

    private fun report(vararg scores: Pair<WheelArea, Double>, estimated: Set<WheelArea> = emptySet()) =
        WheelReport(
            id = "t",
            scores = scores.map { (area, value) ->
                WheelScore(
                    area = area,
                    score = value,
                    source = if (area in estimated) ScoreSource.ESTIMATED else ScoreSource.USER,
                    confidence = if (area in estimated) 0.0 else 1.0,
                    basis = "test",
                )
            },
            generatedAt = LocalDateTime(2026, 8, 4, 9, 0),
        )

    @Test
    fun `the weakest area is offered help`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 8.0,
                WheelArea.MONEY to 3.0,
                WheelArea.FRIENDS to 7.0,
            )
        )

        assertEquals(WheelArea.MONEY, picked)
    }

    @Test
    fun `a wheel that is doing fine is left alone`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 8.0,
                WheelArea.MONEY to 7.0,
                WheelArea.FRIENDS to 9.0,
            )
        )

        // Nothing here needs advice, and offering some anyway is how a prompt becomes furniture.
        assertNull(picked)
    }

    @Test
    fun `a uniformly low wheel is not blamed on one area`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 4.5,
                WheelArea.MONEY to 4.0,
                WheelArea.FRIENDS to 5.0,
            )
        )

        // Someone whose whole wheel sits at 4 does not have a Money problem, they are having a
        // hard time. Picking the lowest of a flat set is arbitrary, and acting on noise is how an
        // app starts feeling like it is guessing.
        assertNull(picked)
    }

    @Test
    fun `a uniformly bad wheel is not blamed on one area either`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 3.0,
                WheelArea.MONEY to 3.0,
                WheelArea.FRIENDS to 3.0,
            )
        )

        // The old guard only held above STRUGGLING, so the flat-wheel rule switched off exactly
        // when the whole wheel was low, and an all-3 wheel crowned whichever area sorted first.
        assertNull(picked)
    }

    @Test
    fun `a genuinely struggling area is still surfaced from a low wheel`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 4.5,
                WheelArea.MONEY to 1.5,
                WheelArea.FRIENDS to 5.0,
            )
        )

        // The spread is wider here and the low end is genuinely bad, so it is not noise.
        assertEquals(WheelArea.MONEY, picked)
    }

    @Test
    fun `estimated areas are never nudged about`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 8.0,
                WheelArea.ROMANCE to 5.0,
                estimated = setOf(WheelArea.ROMANCE),
            )
        )

        // Romance is the lowest number on screen, but it is a placeholder the app invented.
        // Advising someone on a score they never gave us is the app talking to itself.
        assertNull(picked)
    }

    @Test
    fun `nothing measured means nothing to say`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 5.0,
                WheelArea.MONEY to 5.0,
                estimated = setOf(WheelArea.PHYSICAL, WheelArea.MONEY),
            )
        )

        assertNull(picked)
    }

    @Test
    fun `urgency rises as the score falls`() {
        val r = report(
            WheelArea.MONEY to 2.0,
            WheelArea.PHYSICAL to 5.0,
            WheelArea.FRIENDS to 6.0,
        )

        // The register has to match. A cheerful tip aimed at a 2 reads as not paying attention.
        assertEquals(NudgeUrgency.SERIOUS, WheelNudgePicker.urgency(r, WheelArea.MONEY))
        assertEquals(NudgeUrgency.MODERATE, WheelNudgePicker.urgency(r, WheelArea.PHYSICAL))
        assertEquals(NudgeUrgency.GENTLE, WheelNudgePicker.urgency(r, WheelArea.FRIENDS))
    }

    @Test
    fun `Joy is never the area picked`() {
        val picked = WheelNudgePicker.pick(
            report(
                WheelArea.PHYSICAL to 8.0,
                WheelArea.JOY to 1.0,
            )
        )

        // Joy is a reading of the whole wheel, not a slice with actions of its own. "Have more
        // joy" is not advice.
        assertNull(picked)
    }
}
