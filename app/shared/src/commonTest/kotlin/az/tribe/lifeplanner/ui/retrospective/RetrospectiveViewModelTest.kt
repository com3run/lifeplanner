package az.tribe.lifeplanner.ui.retrospective

import app.cash.turbine.test
import az.tribe.lifeplanner.testutil.FakeAiProxyService
import az.tribe.lifeplanner.testutil.FakeRetrospectiveRepository
import az.tribe.lifeplanner.testutil.testDaySnapshot
import az.tribe.lifeplanner.testutil.testFocusSession
import az.tribe.lifeplanner.testutil.testJournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RetrospectiveViewModelTest {

    private lateinit var viewModel: RetrospectiveViewModel
    private lateinit var fakeRepository: FakeRetrospectiveRepository
    private lateinit var fakeAiProxy: FakeAiProxyService
    private val testDispatcher = StandardTestDispatcher()

    private val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeRetrospectiveRepository()
        fakeAiProxy = FakeAiProxyService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RetrospectiveViewModel {
        return RetrospectiveViewModel(repository = fakeRepository, aiProxy = fakeAiProxy)
    }

    // ─── Initial State ───────────────────────────────────────────────────────

    @Test
    fun `initial selected date is yesterday`() = runTest(testDispatcher) {
        // The VM opens on yesterday only when yesterday has activity (else today),
        // so seed an active snapshot for it.
        fakeRepository.snapshotToReturn = testDaySnapshot(
            date = today.minus(DatePeriod(days = 1)),
            journalEntries = listOf(testJournalEntry())
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val expected = today.minus(DatePeriod(days = 1))
        assertEquals(expected, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `initial snapshot is loaded on init`() = runTest(testDispatcher) {
        val snapshot = testDaySnapshot(
            date = today.minus(DatePeriod(days = 1)),
            journalEntries = listOf(testJournalEntry())
        )
        fakeRepository.snapshotToReturn = snapshot

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.snapshot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `today snapshot is loaded eagerly on init`() = runTest(testDispatcher) {
        val todaySnapshot = testDaySnapshot(
            date = today,
            focusSessions = listOf(testFocusSession())
        )
        fakeRepository.snapshotToReturn = todaySnapshot

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.todaySnapshot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── selectDate ──────────────────────────────────────────────────────────

    @Test
    fun `selectDate updates selectedDate and loads snapshot`() = runTest(testDispatcher) {
        val targetDate = today.minus(DatePeriod(days = 3))
        val snapshot = testDaySnapshot(date = targetDate)
        fakeRepository.snapshotToReturn = snapshot

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDate(targetDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(targetDate, state.selectedDate)
            assertNotNull(state.snapshot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectDate ignores future dates`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val originalDate = viewModel.uiState.value.selectedDate
        val futureDate = today.plus(DatePeriod(days = 1))

        viewModel.selectDate(futureDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(originalDate, viewModel.uiState.value.selectedDate)
    }

    // ─── goToPreviousDay / goToNextDay ───────────────────────────────────────

    @Test
    fun `goToPreviousDay moves back one day`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val before = viewModel.uiState.value.selectedDate

        viewModel.goToPreviousDay()
        testDispatcher.scheduler.advanceUntilIdle()

        val after = viewModel.uiState.value.selectedDate
        assertEquals(before.minus(DatePeriod(days = 1)), after)
    }

    @Test
    fun `goToNextDay moves forward one day when not at today`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Go back two days first so we have room to go forward
        viewModel.goToPreviousDay()
        testDispatcher.scheduler.advanceUntilIdle()
        val before = viewModel.uiState.value.selectedDate

        viewModel.goToNextDay()
        testDispatcher.scheduler.advanceUntilIdle()

        val after = viewModel.uiState.value.selectedDate
        assertEquals(before.plus(DatePeriod(days = 1)), after)
    }

    @Test
    fun `goToNextDay does not exceed today`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Default is yesterday; goToNextDay brings us to today
        viewModel.goToNextDay()
        testDispatcher.scheduler.advanceUntilIdle()

        // Now goToNextDay should not move past today
        viewModel.goToNextDay()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selectedDate <= today)
    }

    // ─── loadActiveDatesForMonth ─────────────────────────────────────────────

    @Test
    fun `loadActiveDatesForMonth populates activeDates`() = runTest(testDispatcher) {
        val dates = setOf(
            today.minus(DatePeriod(days = 1)),
            today.minus(DatePeriod(days = 3)),
            today.minus(DatePeriod(days = 5))
        )
        fakeRepository.activeDates = dates

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadActiveDatesForMonth(today)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(dates, state.activeDates)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
