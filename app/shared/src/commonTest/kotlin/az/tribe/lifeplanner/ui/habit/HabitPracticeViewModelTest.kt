package az.tribe.lifeplanner.ui.habit

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.testutil.FakeAbilityRepository
import az.tribe.lifeplanner.testutil.FakeGamificationRepository
import az.tribe.lifeplanner.testutil.FakeHabitRepository
import az.tribe.lifeplanner.testutil.testHabit
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitPracticeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var habits: FakeHabitRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        habits = FakeHabitRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(habitId: String = "habit-1") = HabitPracticeViewModel(
        habitId = habitId,
        habitRepository = habits,
        awardHabitCompletion = AwardHabitCompletionUseCase(
            habitRepository = habits,
            gamificationRepository = FakeGamificationRepository(),
            awardAbilityXpUseCase = AwardAbilityXpUseCase(FakeAbilityRepository()),
            settings = MapSettings(),
        ),
    )

    @Test
    fun `a counted habit loads as a rep counter`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1", title = "Pushups", targetCount = 3, unit = "reps")))

        val state = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.mode).isEqualTo(HabitTrackMode.COUNT)
        assertThat(state.targetReps).isEqualTo(3)
        assertThat(state.reps).isEqualTo(0)
    }

    @Test
    fun `each rep counts up and the last one completes the habit with XP`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1", targetCount = 2, unit = "reps")))
        val vm = viewModel()

        vm.events.test {
            vm.onAction(HabitPracticeAction.OnAddRepClick)
            assertThat(vm.state.value.reps).isEqualTo(1)
            assertThat(vm.state.value.done).isFalse()

            vm.onAction(HabitPracticeAction.OnAddRepClick)
            assertThat(vm.state.value.reps).isEqualTo(2)
            assertThat(vm.state.value.done).isTrue()
            assertThat(awaitItem()).isInstanceOf<HabitPracticeEvent.ShowSnackbar>()
        }
    }

    @Test
    fun `a rep after completion is ignored`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1", targetCount = 2, unit = "reps")))
        val vm = viewModel()

        repeat(3) { vm.onAction(HabitPracticeAction.OnAddRepClick) }

        assertThat(vm.state.value.reps).isEqualTo(2)
    }

    @Test
    fun `marking done checks the habit in for today`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.onAction(HabitPracticeAction.OnCompleteClick)

        assertThat(vm.state.value.done).isTrue()
        assertThat(habits.getCheckInsByHabitId("habit-1").any { it.completed }).isTrue()
    }

    @Test
    fun `back leaves as an event`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.events.test {
            vm.onAction(HabitPracticeAction.OnBackClick)
            assertThat(awaitItem()).isEqualTo(HabitPracticeEvent.NavigateBack)
        }
    }
}
