package az.tribe.lifeplanner.ui.wheel

import az.tribe.lifeplanner.domain.model.ComparisonPeriod
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.WheelScore
import az.tribe.lifeplanner.domain.model.WheelSnapshot
import az.tribe.lifeplanner.domain.repository.WheelRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WheelSetupPromptTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var settings: MapSettings
    private lateinit var repository: FakeWheelRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings = MapSettings()
        repository = FakeWheelRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun score(area: WheelArea, source: ScoreSource) = WheelScore(
        area = area,
        score = 5.0,
        source = source,
        confidence = if (source == ScoreSource.USER) 1.0 else 0.4,
        basis = "test",
    )

    private fun report(vararg scores: WheelScore) = WheelReport(
        id = "r",
        scores = scores.toList(),
        generatedAt = LocalDateTime(2026, 8, 4, 9, 0),
    )

    @Test
    fun `a wheel made entirely of predictions offers the prompt`() = runTest {
        repository.report.value = report(
            score(WheelArea.MONEY, ScoreSource.PREDICTED),
            score(WheelArea.MENTAL, ScoreSource.ESTIMATED),
        )

        val vm = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()

        // Someone who signed up before the wheel moved into registration is looking at numbers we
        // invented, and nothing else on the screen tells them that.
        assertTrue(vm.state.value.showSetupPrompt)
    }

    @Test
    fun `a wheel the user has touched is left alone`() = runTest {
        repository.report.value = report(
            score(WheelArea.MONEY, ScoreSource.USER),
            score(WheelArea.MENTAL, ScoreSource.PREDICTED),
        )

        val vm = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()

        // One real answer means they already know the wheel is theirs to set.
        assertFalse(vm.state.value.showSetupPrompt)
    }

    @Test
    fun `dismissing it means never again`() = runTest {
        repository.report.value = report(score(WheelArea.MONEY, ScoreSource.PREDICTED))
        val vm = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()

        vm.dismissSetupPrompt()
        testScheduler.advanceUntilIdle()
        assertFalse(vm.state.value.showSetupPrompt)

        // A prompt that comes back after "not now" is the exact furniture problem this pattern is
        // supposed to avoid, and this one sits on top of the thing it is about.
        val reopened = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()
        assertFalse(reopened.state.value.showSetupPrompt)
    }

    @Test
    fun `answering writes every score and closes the prompt for good`() = runTest {
        repository.report.value = report(score(WheelArea.MONEY, ScoreSource.PREDICTED))
        val vm = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()

        vm.setScores(mapOf(WheelArea.MONEY to 3.0, WheelArea.FRIENDS to 8.0))
        testScheduler.advanceUntilIdle()

        assertEquals(3.0, repository.written[WheelArea.MONEY])
        assertEquals(8.0, repository.written[WheelArea.FRIENDS])
        assertFalse(vm.state.value.showSetupPrompt)
        // A first day on record, or the first comparison has nothing to measure against.
        assertTrue(repository.snapshots > 0)
    }

    @Test
    fun `an empty wheel does not prompt about nothing`() = runTest {
        repository.report.value = report()

        val vm = WheelViewModel(repository, settings)
        testScheduler.advanceUntilIdle()

        // No scores at all is a wheel that has not been computed yet, not a wheel full of guesses.
        // Offering to correct nothing would be the app talking to itself.
        assertFalse(vm.state.value.showSetupPrompt)
    }

    private class FakeWheelRepository : WheelRepository {
        val report = MutableStateFlow(
            WheelReport(id = "r", scores = emptyList(), generatedAt = LocalDateTime(2026, 8, 4, 9, 0))
        )
        val written = mutableMapOf<WheelArea, Double>()
        var snapshots = 0

        override fun observeWheel(): Flow<WheelReport> = report
        override suspend fun getWheel(): WheelReport = report.value
        override suspend fun setScore(area: WheelArea, score: Double, note: String?) {
            written[area] = score
        }
        override suspend fun clearScore(area: WheelArea) { written.remove(area) }
        override suspend fun captureSnapshot() { snapshots++ }
        override suspend fun snapshots(): List<WheelSnapshot> = emptyList()
        override suspend fun compareTo(period: ComparisonPeriod) = null
        override suspend fun compareToDate(date: LocalDate) = null
    }
}
