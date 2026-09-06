package az.tribe.lifeplanner.ui.habit

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import az.tribe.lifeplanner.testutil.FakeAbilityRepository
import az.tribe.lifeplanner.testutil.FakeGamificationRepository
import az.tribe.lifeplanner.testutil.FakeGoalRepository
import az.tribe.lifeplanner.testutil.FakeHabitRepository
import az.tribe.lifeplanner.testutil.FakeKnowledgeRepository
import az.tribe.lifeplanner.testutil.testGoal
import az.tribe.lifeplanner.testutil.testHabit
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.RecommendLessonsForHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
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
class HabitDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var habits: FakeHabitRepository
    private lateinit var goals: FakeGoalRepository
    private lateinit var gamification: FakeGamificationRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        habits = FakeHabitRepository()
        goals = FakeGoalRepository()
        gamification = FakeGamificationRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(habitId: String = "habit-1") = HabitDetailViewModel(
        habitId = habitId,
        habitRepository = habits,
        goalRepository = goals,
        checkInHabitUseCase = CheckInHabitUseCase(habits),
        uncheckHabitUseCase = UncheckHabitUseCase(habits),
        awardHabitCompletionUseCase = AwardHabitCompletionUseCase(
            habitRepository = habits,
            gamificationRepository = gamification,
            awardAbilityXpUseCase = AwardAbilityXpUseCase(FakeAbilityRepository()),
            settings = MapSettings(),
        ),
        recommendLessonsForHabit = RecommendLessonsForHabitUseCase(FakeKnowledgeRepository(), gamification),
    )

    @Test
    fun `the habit, today's status and its goal land in one state`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1", title = "Run", linkedGoalId = "goal-1")))
        goals.setGoals(listOf(testGoal(id = "goal-1", title = "Half marathon")))

        val state = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.habit?.title).isEqualTo("Run")
        assertThat(state.doneToday).isFalse()
        assertThat(state.linkedGoalTitle).isEqualTo("Half marathon")
    }

    @Test
    fun `a habit that does not exist stops loading with nothing to show`() = runTest {
        val state = viewModel(habitId = "missing").state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.habit).isNull()
    }

    @Test
    fun `toggling today checks the habit in and announces the XP`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.events.test {
            vm.onAction(HabitDetailAction.OnToggleTodayClick)
            assertThat(awaitItem()).isInstanceOf<HabitDetailEvent.ShowSnackbar>()
        }
        assertThat(vm.state.value.doneToday).isTrue()
    }

    @Test
    fun `toggling again undoes today's check-in`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.onAction(HabitDetailAction.OnToggleTodayClick)
        vm.onAction(HabitDetailAction.OnToggleTodayClick)

        assertThat(vm.state.value.doneToday).isFalse()
    }

    @Test
    fun `tapping a lesson navigates to it`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.events.test {
            vm.onAction(HabitDetailAction.OnLessonClick("lesson-9"))
            assertThat(awaitItem()).isEqualTo(HabitDetailEvent.NavigateToLesson("lesson-9"))
        }
    }

    @Test
    fun `back and practice leave as events, never as state`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1")))
        val vm = viewModel()

        vm.events.test {
            vm.onAction(HabitDetailAction.OnBackClick)
            assertThat(awaitItem()).isEqualTo(HabitDetailEvent.NavigateBack)
            vm.onAction(HabitDetailAction.OnPracticeClick)
            assertThat(awaitItem()).isEqualTo(HabitDetailEvent.NavigateToPractice("habit-1"))
        }
    }

    @Test
    fun `edit opens the sheet and confirming saves the habit and closes it`() = runTest {
        habits.setHabits(listOf(testHabit(id = "habit-1", title = "Run")))
        val vm = viewModel()

        vm.onAction(HabitDetailAction.OnEditClick)
        assertThat(vm.state.value.isEditing).isTrue()

        vm.onAction(HabitDetailAction.OnEditConfirm(vm.state.value.habit!!.copy(title = "Run far")))

        assertThat(vm.state.value.isEditing).isFalse()
        assertThat(habits.getHabitById("habit-1")?.title).isEqualTo("Run far")
        assertThat(vm.state.value.habit?.title).isEqualTo("Run far")
    }
}
