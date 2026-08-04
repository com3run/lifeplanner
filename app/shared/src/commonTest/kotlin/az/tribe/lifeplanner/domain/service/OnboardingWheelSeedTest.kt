package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.WheelArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingWheelSeedTest {

    private fun full(vararg overrides: Pair<WheelArea, Double>): Map<WheelArea, Double> =
        WheelArea.segments().associateWith { 7.0 } + overrides.toMap()

    @Test
    fun `the weakest areas become the priorities`() {
        val priorities = OnboardingWheelSeed.prioritiesFrom(
            full(WheelArea.MONEY to 2.0, WheelArea.PHYSICAL to 3.0, WheelArea.FRIENDS to 4.0)
        )

        // The user has already told us where the work is, so asking them again from a blank list
        // is a question we do not need to put them through.
        assertEquals(listOf(GoalCategory.MONEY, GoalCategory.BODY, GoalCategory.PEOPLE), priorities)
    }

    @Test
    fun `Mission and Growth do not spend two priority slots on Career`() {
        val priorities = OnboardingWheelSeed.prioritiesFrom(
            full(WheelArea.MISSION to 1.0, WheelArea.GROWTH to 2.0, WheelArea.MONEY to 3.0, WheelArea.MENTAL to 4.0)
        )

        // Both map to Career. Without dedup the user would get Career twice and lose a third area.
        assertEquals(listOf(GoalCategory.CAREER, GoalCategory.MONEY, GoalCategory.WELLBEING), priorities)
    }

    @Test
    fun `Romance contributes nothing because no goal category exists for it`() {
        val priorities = OnboardingWheelSeed.prioritiesFrom(
            full(WheelArea.ROMANCE to 1.0, WheelArea.MONEY to 2.0, WheelArea.PHYSICAL to 3.0, WheelArea.FRIENDS to 4.0)
        )

        // Romance is the weakest, but there is no category for a goal to be filed under, so it
        // cannot become a focus without inventing somewhere to put the work.
        assertEquals(3, priorities.size)
        assertEquals(listOf(GoalCategory.MONEY, GoalCategory.BODY, GoalCategory.PEOPLE), priorities)
    }

    @Test
    fun `no ratings means no derived priorities`() {
        // Skipping is allowed, and inventing a focus for someone who told us nothing is worse than
        // having none.
        assertTrue(OnboardingWheelSeed.prioritiesFrom(emptyMap()).isEmpty())
        assertNull(OnboardingWheelSeed.weakestArea(emptyMap()))
    }

    @Test
    fun `ties resolve the same way every time`() {
        val ratings = full(WheelArea.MONEY to 3.0, WheelArea.MENTAL to 3.0, WheelArea.PHYSICAL to 3.0)

        // A flat set must not shuffle between runs, or the user's focus would change on a reload
        // for no reason they could see.
        repeat(5) {
            assertEquals(OnboardingWheelSeed.prioritiesFrom(ratings), OnboardingWheelSeed.prioritiesFrom(ratings))
        }
        assertEquals(WheelArea.FAMILY, OnboardingWheelSeed.weakestArea(full(WheelArea.FAMILY to 1.0)))
    }

    @Test
    fun `the weakest area is the lowest one actually rated`() {
        assertEquals(
            WheelArea.SPIRITUAL,
            OnboardingWheelSeed.weakestArea(full(WheelArea.SPIRITUAL to 1.5)),
        )
    }

    @Test
    fun `Joy is never the weakest area`() {
        val ratings = full() + (WheelArea.JOY to 0.0)

        // Joy reads the whole wheel rather than being a slice you can act on, so opening the
        // conversation with it would be opening it on something the user cannot do anything about.
        assertTrue(OnboardingWheelSeed.weakestArea(ratings) != WheelArea.JOY)
    }

    @Test
    fun `a mostly-answered wheel is worth seeding and a barely-answered one is not`() {
        assertTrue(OnboardingWheelSeed.isEnoughToSeed(full()))

        // Drawing three answers as though they were a wheel would show the user a picture of their
        // life that they did not draw.
        val barely = mapOf(WheelArea.MONEY to 3.0, WheelArea.MENTAL to 4.0, WheelArea.FRIENDS to 5.0)
        assertFalse(OnboardingWheelSeed.isEnoughToSeed(barely))
    }

    @Test
    fun `one skipped area still seeds`() {
        // Being made to answer all nine to get anything would push people to tap through at random,
        // which is worse data than an honest gap.
        val allButOne = full() - WheelArea.ROMANCE
        assertTrue(OnboardingWheelSeed.isEnoughToSeed(allButOne))
    }
}
