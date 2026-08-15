package az.tribe.lifeplanner.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ComparisonPeriod
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelComparison
import az.tribe.lifeplanner.domain.model.WheelDelta
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.WheelScore
import az.tribe.lifeplanner.domain.service.NudgeUrgency
import az.tribe.lifeplanner.domain.service.WheelNudgePicker
import az.tribe.lifeplanner.domain.service.WheelSuggestions
import az.tribe.lifeplanner.ui.onboarding.WheelRatingStep
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import az.tribe.lifeplanner.ui.wheel.WheelCanvas
import az.tribe.lifeplanner.ui.wheel.WheelFace
import az.tribe.lifeplanner.ui.wheel.WheelHistoryCard
import az.tribe.lifeplanner.ui.wheel.WheelSetupPromptCard
import az.tribe.lifeplanner.ui.wheel.WheelSetupSheet
import az.tribe.lifeplanner.ui.wheel.WheelStripCard
import az.tribe.lifeplanner.ui.wheel.WheelSuggestionCard
import az.tribe.lifeplanner.ui.wheel.wheelMood
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
 * Screenshot previews for the Wheel of Life surfaces. Same harness as [PreviewScreenshots];
 * PNGs land in app/shared/build/previews/.
 *
 * Run: ./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.previews.WheelPreviews"
 *
 * The fixture wheel is deliberately lopsided (9s beside 3s, mixed sources) because the point of
 * the wheel is its shape; a flat wheel would hide most of what these screens do.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class WheelPreviews {

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
        compose.mainClock.advanceTimeBy(800)
        compose.onRoot().captureRoboImage("build/previews/$name.png")
    }

    private fun score(
        area: WheelArea,
        value: Double,
        source: ScoreSource = ScoreSource.PREDICTED,
        confidence: Double = 0.7,
        basis: String,
    ) = WheelScore(area = area, score = value, source = source, confidence = confidence, basis = basis)

    private val report = WheelReport(
        id = "preview-wheel",
        scores = listOf(
            score(WheelArea.MISSION, 7.5, basis = "2 career goals moving, 12 focus sessions this month"),
            score(WheelArea.FAMILY, 8.0, ScoreSource.USER, 1.0, "You set this yourself"),
            score(WheelArea.FRIENDS, 3.5, confidence = 0.6, basis = "No social plans or habits in 60 days"),
            score(WheelArea.ROMANCE, 6.0, ScoreSource.ESTIMATED, 0.2, "Nothing to go on; started from the middle"),
            score(WheelArea.SPIRITUAL, 4.5, ScoreSource.ESTIMATED, 0.3, "One journal mention in 30 days"),
            score(WheelArea.MENTAL, 6.5, basis = "Mood averaging 3.4 of 5 this month"),
            score(WheelArea.PHYSICAL, 7.0, ScoreSource.USER, 1.0, "You set this yourself"),
            score(WheelArea.GROWTH, 5.5, confidence = 0.6, basis = "1 learning habit, streak 4 days"),
            score(WheelArea.MONEY, 4.0, basis = "2 money goals untouched for 3 weeks"),
            score(WheelArea.JOY, 6.0, confidence = 0.5, basis = "Read from the rest of the wheel plus mood"),
        ),
        generatedAt = LocalDateTime(2026, 8, 14, 9, 0),
    )

    private val weekComparison = WheelComparison(
        period = ComparisonPeriod.WEEK,
        previousDate = LocalDate(2026, 8, 7),
        currentDate = LocalDate(2026, 8, 14),
        deltas = listOf(
            WheelDelta(WheelArea.PHYSICAL, from = 6.0, to = 7.0),
            WheelDelta(WheelArea.FRIENDS, from = 3.0, to = 3.5),
            WheelDelta(WheelArea.MONEY, from = 4.5, to = 4.0),
            WheelDelta(WheelArea.MENTAL, from = 6.5, to = 6.5),
        ),
        newlyScored = listOf(WheelArea.ROMANCE),
    )

    @Test
    fun wheelCanvas() = snap("Wheel_canvas") {
        WheelCanvas(scores = report.scores, onAreaTap = {}, modifier = Modifier.size(360.dp))
    }

    @Test
    fun wheelCanvasSelected() = snap("Wheel_canvas_selected") {
        WheelCanvas(
            scores = report.scores,
            onAreaTap = {},
            selected = WheelArea.FRIENDS,
            modifier = Modifier.size(360.dp),
        )
    }

    /** The dashed arcs are last week's edges, from the same comparison the history card shows. */
    @Test
    fun wheelCanvasGhost() = snap("Wheel_canvas_ghost") {
        WheelCanvas(
            scores = report.scores,
            onAreaTap = {},
            ghost = weekComparison.movedFrom,
            modifier = Modifier.size(360.dp),
        )
    }

    @Test
    fun wheelCanvasCompact() = snap("Wheel_canvas_compact") {
        WheelCanvas(
            scores = report.scores,
            onAreaTap = {},
            compact = true,
            modifier = Modifier.size(96.dp),
        )
    }

    @Test
    fun wheelStripCard() = snap("Wheel_stripCard") {
        WheelStripCard(report = report, onOpen = {})
    }

    @Test
    fun wheelStripCardProminent() = snap("Wheel_stripCard_prominent") {
        WheelStripCard(report = report, onOpen = {}, prominent = true)
    }

    @Test
    fun wheelHistoryMovement() = snap("Wheel_history_movement") {
        WheelHistoryCard(
            comparison = weekComparison,
            period = ComparisonPeriod.WEEK,
            snapshotCount = 5,
            isLoading = false,
            onPeriodChange = {},
        )
    }

    /**
     * The first-snapshot empty state: there is history, there is just not enough of it yet. No
     * chips either — with nothing to compare, every chip produced the identical sentence, which
     * is how the owner read the card as broken.
     */
    @Test
    fun wheelHistoryFirstSnapshot() = snap("Wheel_history_firstSnapshot") {
        WheelHistoryCard(
            comparison = null,
            period = ComparisonPeriod.WEEK,
            snapshotCount = 1,
            isLoading = false,
            onPeriodChange = {},
            offeredPeriods = emptyList(),
        )
    }

    /** Two days on record: exactly one chip, labelling the stats rather than pretending choice. */
    @Test
    fun wheelHistoryDayOnly() = snap("Wheel_history_dayOnly") {
        WheelHistoryCard(
            comparison = WheelComparison(
                period = ComparisonPeriod.DAY,
                previousDate = LocalDate(2026, 8, 14),
                currentDate = LocalDate(2026, 8, 15),
                deltas = listOf(WheelDelta(WheelArea.FRIENDS, from = 3.0, to = 3.5)),
            ),
            period = ComparisonPeriod.DAY,
            snapshotCount = 2,
            isLoading = false,
            onPeriodChange = {},
            offeredPeriods = listOf(ComparisonPeriod.DAY),
        )
    }

    /**
     * Real authored copy via the real picker, so reviewing this PNG reviews what ships: the
     * urgency comes from the fixture report and the words from [WheelSuggestions], rotation 0.
     */
    @Test
    fun wheelSuggestionFriends() = snap("Wheel_suggestion_friends") {
        val urgency = WheelNudgePicker.urgency(report, WheelArea.FRIENDS)
        WheelSuggestionCard(suggestion = WheelSuggestions.forArea(WheelArea.FRIENDS, urgency))
    }

    /** Serious Mental is the sensitive one: it must point at real help, never rotate. */
    @Test
    fun wheelSuggestionMentalSerious() = snap("Wheel_suggestion_mental_serious") {
        WheelSuggestionCard(suggestion = WheelSuggestions.forArea(WheelArea.MENTAL, NudgeUrgency.SERIOUS))
    }

    @Test
    fun wheelFaces() = snap("Wheel_faces") {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf(2.0, 5.0, 8.0).forEach { value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(110.dp),
                ) {
                    WheelFace(
                        score = value,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = wheelMood(value),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    @Test
    fun wheelSetupPromptCard() = snap("Wheel_setupPromptCard") {
        WheelSetupPromptCard(onStart = {}, onDismiss = {})
    }

    /** Mid-form: three areas answered, so both the answered and unanswered rows are on screen. */
    @Test
    fun wheelRatingStep() = snap("Wheel_ratingStep") {
        WheelRatingStep(
            ratings = mapOf(
                WheelArea.MISSION to 7.0,
                WheelArea.FAMILY to 8.0,
                WheelArea.FRIENDS to 4.0,
            ),
            onRate = { _, _ -> },
            onContinue = {},
            onSkip = {},
        )
    }

    /** ModalBottomSheet renders in its own window, so capture the whole screen. */
    @Test
    fun wheelSetupSheet() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LifePlannerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    WheelSetupSheet(
                        initial = report.segments.associate { it.area to it.score },
                        onDone = {},
                        onDismiss = {},
                    )
                }
            }
        }
        compose.mainClock.advanceTimeBy(1_500)
        captureScreenRoboImage("build/previews/Wheel_setupSheet.png")
    }
}
