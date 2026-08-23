package az.tribe.lifeplanner.previews

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.DecisionSource
import az.tribe.lifeplanner.domain.model.DecisionStatus
import az.tribe.lifeplanner.domain.model.OutcomeQuality
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.ChoicePointDetector
import az.tribe.lifeplanner.testutil.FakeAbilityRepository
import az.tribe.lifeplanner.testutil.FakeDecisionProfileRepository
import az.tribe.lifeplanner.testutil.FakeDecisionRepository
import az.tribe.lifeplanner.testutil.FakeGamificationRepository
import az.tribe.lifeplanner.testutil.FakeGoalRepository
import az.tribe.lifeplanner.testutil.FakeHabitRepository
import az.tribe.lifeplanner.ui.decision.DecisionJournalScreen
import az.tribe.lifeplanner.ui.decision.DecisionViewModel
import az.tribe.lifeplanner.ui.decision.MetacognitiveReviewViewModel
import az.tribe.lifeplanner.ui.theme.LifePlannerTheme
import az.tribe.lifeplanner.usecases.ability.AwardDecisionBadgesUseCase
import az.tribe.lifeplanner.usecases.ability.AwardDecisionXpUseCase
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Whole-screen previews, as opposed to the component shots in [PreviewScreenshots].
 *
 * These render the real screen composable, Scaffold and top bar included, against fake
 * repositories wired through Koin. That is deliberately not a refactor into stateless content:
 * rendering the actual screen means the preview exercises the same code path the device does,
 * so a broken Scaffold or a mis-sized top bar shows up here rather than on a handset.
 *
 * Run: ./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.previews.ScreenPreviews"
 * PNGs land in app/shared/build/previews/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenPreviews {

    @get:Rule
    val compose = createComposeRule()

    private val decisionRepo = FakeDecisionRepository(seedDecisions())

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<DecisionRepository> { decisionRepo }
                    single<GoalRepository> { FakeGoalRepository() }
                    single<HabitRepository> { FakeHabitRepository() }
                    single<GamificationRepository> { FakeGamificationRepository() }
                    single<DecisionProfileRepository> { FakeDecisionProfileRepository() }
                    single<AbilityRepository> { FakeAbilityRepository() }
                    single { ChoicePointDetector() }
                    factory { AwardDecisionXpUseCase(get()) }
                    factory { AwardDecisionBadgesUseCase(get()) }
                    viewModel { DecisionViewModel(get(), get(), get(), get(), get()) }
                    viewModel { MetacognitiveReviewViewModel(get(), get(), get()) }
                }
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    private fun snap(name: String, darkTheme: Boolean, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LifePlannerTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) { content() }
            }
        }
        compose.mainClock.advanceTimeBy(800)
        compose.onRoot().captureRoboImage("build/previews/$name.png")
    }

    @Test
    fun decisionsLight() = snap("screen_decisions_light", darkTheme = false) {
        DecisionJournalScreen(onBackClick = {}, onDecisionClick = {})
    }

    @Test
    fun decisionsDark() = snap("screen_decisions_dark", darkTheme = true) {
        DecisionJournalScreen(onBackClick = {}, onDecisionClick = {})
    }
}

/**
 * A log with enough shape to exercise the track record: reviewed calls on both sides of the
 * process/luck split, and unreviewed ones so "still owed" is not zero. Fixed dates keep reruns
 * comparable.
 */
private fun seedDecisions(): List<Decision> = listOf(
    Decision(
        id = "d1",
        question = "Should I drop the Tuesday gym slot and move it to mornings?",
        chosenOption = "Move to mornings",
        reasoning = "Evenings kept losing to work overruns.",
        confidence = 80,
        decidedAt = LocalDateTime(2026, 7, 2, 9, 0),
        outcomeQuality = OutcomeQuality.GOOD_PROCESS_GOOD_RESULT,
        outcomeReviewedAt = LocalDateTime(2026, 8, 1, 9, 0),
    ),
    Decision(
        id = "d2",
        question = "Take the contract or hold out for the bigger one?",
        chosenOption = "Took the contract",
        reasoning = "Cash now beat a maybe in six weeks.",
        confidence = 90,
        decidedAt = LocalDateTime(2026, 7, 9, 11, 0),
        outcomeQuality = OutcomeQuality.GOOD_PROCESS_BAD_RESULT,
        outcomeReviewedAt = LocalDateTime(2026, 8, 3, 9, 0),
    ),
    Decision(
        id = "d3",
        question = "Ship the redesign before the trip?",
        chosenOption = "Shipped it",
        reasoning = "Wanted it out of my head before flying.",
        confidence = 85,
        decidedAt = LocalDateTime(2026, 7, 15, 16, 0),
        outcomeQuality = OutcomeQuality.BAD_PROCESS_GOOD_RESULT,
        outcomeReviewedAt = LocalDateTime(2026, 8, 5, 9, 0),
    ),
    Decision(
        id = "d4",
        question = "Cut the Sunday long run to protect the knee?",
        chosenOption = "Cut it",
        confidence = 60,
        decidedAt = LocalDateTime(2026, 7, 26, 8, 0),
    ),
    Decision(
        id = "d5",
        question = "Move the launch to September?",
        chosenOption = "Held the date",
        confidence = 70,
        decidedAt = LocalDateTime(2026, 8, 10, 10, 0),
    ),
    Decision(
        id = "d6",
        question = "Was skipping the retro a mistake?",
        chosenOption = "Skipped it",
        confidence = 40,
        decidedAt = LocalDateTime(2026, 8, 14, 15, 0),
        source = DecisionSource.JOURNAL,
        status = DecisionStatus.PENDING,
    ),
)
