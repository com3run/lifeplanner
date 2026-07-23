package az.tribe.lifeplanner.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import az.tribe.lifeplanner.domain.model.Badge
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.ui.components.BadgeCard
import az.tribe.lifeplanner.ui.components.BadgeMedallion
import az.tribe.lifeplanner.ui.goal.GoalJourneyCard
import az.tribe.lifeplanner.ui.intro.FeatureIntro
import az.tribe.lifeplanner.ui.intro.FeatureIntroSheet
import az.tribe.lifeplanner.ui.intro.IntroBenefit
import az.tribe.lifeplanner.ui.intro.IntroIcon
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * JVM screenshot previews: renders commonMain composables with Robolectric native graphics,
 * no emulator or device needed. PNGs land in app/shared/build/previews/.
 *
 * Run: ./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.previews.PreviewScreenshots"
 *
 * Workflow: when changing a component, add or adjust a preview here, render, and review the
 * PNG before building for a device. Keep fixtures deterministic (fixed dates, isNew = false)
 * so reruns produce comparable images.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PreviewScreenshots {

    @get:Rule
    val compose = createComposeRule()

    private fun snap(name: String, darkTheme: Boolean = true, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LifePlannerTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.padding(16.dp)) { content() }
                }
            }
        }
        // Advance past entrance animations to a stable frame; infinite transitions
        // (badge wobble) stay frozen because autoAdvance is off.
        compose.mainClock.advanceTimeBy(800)
        compose.onRoot().captureRoboImage("build/previews/$name.png")
    }

    private val earnedBadge = Badge(
        id = "preview-badge",
        type = BadgeType.STREAK_7,
        earnedAt = LocalDateTime(2026, 7, 20, 8, 0),
        isNew = false,
    )

    private fun goal(milestonesDone: Int, status: GoalStatus = GoalStatus.IN_PROGRESS) = Goal(
        id = "preview-goal",
        category = GoalCategory.BODY,
        title = "Run a half marathon",
        description = "Train up to 21K by mid October",
        status = status,
        timeline = GoalTimeline.MID_TERM,
        dueDate = LocalDate(2026, 10, 15),
        milestones = listOf(
            Milestone("m1", "Run 5K without stopping", isCompleted = milestonesDone >= 1),
            Milestone("m2", "Finish a 10K race", isCompleted = milestonesDone >= 2),
            Milestone("m3", "Complete the half marathon", isCompleted = milestonesDone >= 3),
        ),
        createdAt = LocalDateTime(2026, 5, 1, 9, 0),
        completionRate = milestonesDone / 3f,
    )

    @Test
    fun badgeCards() = snap("BadgeCard") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(
                badge = earnedBadge,
                badgeType = BadgeType.STREAK_7,
                isEarned = true,
                modifier = Modifier.width(130.dp),
            )
            BadgeCard(
                badge = null,
                badgeType = BadgeType.FOCUS_10,
                isEarned = false,
                modifier = Modifier.width(130.dp),
            )
        }
    }

    @Test
    fun badgeMedallions() = snap("BadgeMedallion") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeMedallion(type = BadgeType.STREAK_7, isEarned = true, size = 56.dp)
            BadgeMedallion(type = BadgeType.FIRST_STEP, isEarned = true, size = 56.dp)
            BadgeMedallion(type = BadgeType.FOCUS_10, isEarned = false, size = 56.dp)
        }
    }

    @Test
    fun goalJourneyMidway() = snap("GoalJourneyCard_midway") {
        GoalJourneyCard(goal = goal(milestonesDone = 1), horizontalPadding = 0.dp)
    }

    @Test
    fun goalJourneyFinalStep() = snap("GoalJourneyCard_finalStep") {
        GoalJourneyCard(goal = goal(milestonesDone = 2), horizontalPadding = 0.dp)
    }

    @Test
    fun goalJourneyComplete() = snap("GoalJourneyCard_complete") {
        GoalJourneyCard(
            goal = goal(milestonesDone = 3, status = GoalStatus.COMPLETED),
            horizontalPadding = 0.dp,
        )
    }

    @Test
    fun goalJourneyLight() = snap("GoalJourneyCard_light", darkTheme = false) {
        GoalJourneyCard(goal = goal(milestonesDone = 1), horizontalPadding = 0.dp)
    }

    @Test
    fun featureIntroSheet() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LifePlannerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FeatureIntroSheet(
                        intro = FeatureIntro(
                            id = "intro_preview",
                            icon = IntroIcon.COMPASS,
                            eyebrow = "YOUR COMPASS",
                            title = "Name what matters to you",
                            whatItIs = "A short list of values that every goal you set can point back to.",
                            benefits = listOf(
                                IntroBenefit(IntroIcon.COMPASS, "Your goals show the reason behind them, not just a due date."),
                                IntroBenefit(IntroIcon.TARGET, "The coach uses your values when it helps you plan."),
                            ),
                            asks = "Two minutes to pick a few values.",
                            ctaLabel = "Pick my values",
                        ),
                        accent = MaterialTheme.colorScheme.primary,
                        onDismiss = {},
                        onContinue = {},
                    )
                }
            }
        }
        // The sheet animates in inside a dialog window, so capture the whole screen.
        compose.mainClock.advanceTimeBy(1_500)
        captureScreenRoboImage("build/previews/FeatureIntroSheet.png")
    }
}
