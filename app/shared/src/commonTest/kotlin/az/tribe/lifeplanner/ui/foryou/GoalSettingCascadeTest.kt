package az.tribe.lifeplanner.ui.foryou

import az.tribe.lifeplanner.ui.foryou.GoalSettingCascade.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GoalSettingCascadeTest {

    @Test
    fun visionLeadsWhenValuesSurfaceIsOnAndEmpty() {
        assertEquals(
            Step.VISION,
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = false,
                questAgeDays = null,
                daysSinceLastReview = null,
            ),
        )
    }

    @Test
    fun cascadeStartsAtQuestWhenValuesSurfaceIsOff() {
        assertEquals(
            Step.QUEST,
            GoalSettingCascade.nextStep(
                valuesEnabled = false,
                hasActiveValues = false,
                questAgeDays = null,
                daysSinceLastReview = null,
            ),
        )
    }

    @Test
    fun questFollowsOnceValuesExist() {
        assertEquals(
            Step.QUEST,
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = null,
                daysSinceLastReview = null,
            ),
        )
    }

    @Test
    fun missingQuestOutranksAStaleReview() {
        assertEquals(
            Step.QUEST,
            GoalSettingCascade.nextStep(
                valuesEnabled = false,
                hasActiveValues = false,
                questAgeDays = null,
                daysSinceLastReview = 30,
            ),
        )
    }

    @Test
    fun firstReviewWaitsUntilTheQuestIsAWeekOld() {
        assertNull(
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = 3,
                daysSinceLastReview = null,
            ),
        )
    }

    @Test
    fun firstReviewInvitedOnceTheQuestIsAWeekOld() {
        assertEquals(
            Step.WEEKLY_REVIEW,
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = 7,
                daysSinceLastReview = null,
            ),
        )
    }

    @Test
    fun reviewReturnsOnAWeeklyCadence() {
        assertEquals(
            Step.WEEKLY_REVIEW,
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = 40,
                daysSinceLastReview = 8,
            ),
        )
    }

    @Test
    fun cascadeIsSatisfiedAfterARecentReview() {
        assertNull(
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = 40,
                daysSinceLastReview = 2,
            ),
        )
    }

    @Test
    fun freshQuestDoesNotBlockAnEstablishedReviewCadence() {
        assertEquals(
            Step.WEEKLY_REVIEW,
            GoalSettingCascade.nextStep(
                valuesEnabled = true,
                hasActiveValues = true,
                questAgeDays = 1,
                daysSinceLastReview = 9,
            ),
        )
    }
}
