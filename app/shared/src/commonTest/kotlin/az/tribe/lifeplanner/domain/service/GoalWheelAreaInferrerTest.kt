package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.WheelArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoalWheelAreaInferrerTest {

    @Test
    fun `every category resolves to an area`() {
        // The whole point of replacing the value inferrer: a tag that is usually absent cannot
        // connect a goal to a score. There is no null return and no goal without a why.
        GoalCategory.entries.forEach { category ->
            val area = GoalWheelAreaInferrer.infer(category, "some goal", "")
            assertTrue(area in WheelArea.entries, "$category produced nothing usable")
        }
    }

    @Test
    fun `a career goal is about the work by default`() {
        assertEquals(
            WheelArea.MISSION,
            GoalWheelAreaInferrer.infer(GoalCategory.CAREER, "Get promoted to Senior Designer"),
        )
    }

    @Test
    fun `a career goal about getting better lands in Growth`() {
        // Mission and Growth have different rubrics: one is "the work matters", the other is
        // "you are better than you were". A certification is the second.
        assertEquals(
            WheelArea.GROWTH,
            GoalWheelAreaInferrer.infer(GoalCategory.CAREER, "Complete Google UX Design certificate"),
        )
    }

    @Test
    fun `a social goal about a partner is Romance, not Friends`() {
        // Romance has no goal category of its own, so without the text it would silently land in
        // Friends and quietly corrupt two areas of the wheel at once.
        assertEquals(
            WheelArea.ROMANCE,
            GoalWheelAreaInferrer.infer(GoalCategory.PEOPLE, "Plan a weekend away with my partner"),
        )
    }

    @Test
    fun `a social goal about relatives is Family`() {
        assertEquals(
            WheelArea.FAMILY,
            GoalWheelAreaInferrer.infer(GoalCategory.PEOPLE, "Call my mum every Sunday"),
        )
    }

    @Test
    fun `an ordinary social goal is Friends`() {
        assertEquals(
            WheelArea.FRIENDS,
            GoalWheelAreaInferrer.infer(GoalCategory.PEOPLE, "See people more often"),
        )
    }

    @Test
    fun `a family-category goal about a spouse is still Romance`() {
        // The user filed it under Family, which is reasonable, but the wheel asks a different
        // question about a marriage than it does about parents and siblings.
        assertEquals(
            WheelArea.ROMANCE,
            GoalWheelAreaInferrer.infer(GoalCategory.FAMILY, "Take my wife somewhere every month"),
        )
    }

    @Test
    fun `the unambiguous categories map straight through`() {
        assertEquals(WheelArea.MONEY, GoalWheelAreaInferrer.infer(GoalCategory.MONEY, "Save 5000"))
        assertEquals(WheelArea.PHYSICAL, GoalWheelAreaInferrer.infer(GoalCategory.BODY, "Run a marathon"))
        assertEquals(WheelArea.MENTAL, GoalWheelAreaInferrer.infer(GoalCategory.WELLBEING, "Worry less"))
        assertEquals(WheelArea.SPIRITUAL, GoalWheelAreaInferrer.infer(GoalCategory.PURPOSE, "Explore Spirituality"))
    }

    @Test
    fun `the description counts, not just the title`() {
        // Titles are often terse. "Level up" says nothing; the description is where the user
        // explained themselves.
        assertEquals(
            WheelArea.GROWTH,
            GoalWheelAreaInferrer.infer(
                GoalCategory.CAREER,
                "Level up",
                "Finish the advanced course and actually apply the skill at work",
            ),
        )
    }

    @Test
    fun `Joy is never inferred and never offered`() {
        // Joy is a reading of the whole wheel, not a slice with goals of its own. A goal tagged to
        // it would put a number on the wheel that nothing the user does can move.
        GoalCategory.entries.forEach { category ->
            assertTrue(GoalWheelAreaInferrer.infer(category, "be happy", "joy and happiness") != WheelArea.JOY)
        }
        assertFalse(WheelArea.JOY in GoalWheelAreaInferrer.selectable)
    }

    @Test
    fun `the picker offers every area a goal can actually be tagged to`() {
        // Anything infer() can return has to be pickable, or a user could be shown an area they
        // cannot choose again after changing it once.
        val inferable = GoalCategory.entries.flatMap { category ->
            listOf(
                GoalWheelAreaInferrer.infer(category, "learn a language"),
                GoalWheelAreaInferrer.infer(category, "time with my partner"),
                GoalWheelAreaInferrer.infer(category, "call my dad"),
                GoalWheelAreaInferrer.infer(category, "plain goal"),
            )
        }.toSet()

        inferable.forEach {
            assertTrue(it in GoalWheelAreaInferrer.selectable, "$it can be inferred but not picked")
        }
    }

    @Test
    fun `matching is case-insensitive`() {
        assertEquals(
            WheelArea.ROMANCE,
            GoalWheelAreaInferrer.infer(GoalCategory.PEOPLE, "Date Night With My WIFE"),
        )
    }
}
