package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.enum.HabitCompletionSource
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.testutil.FakeAbilityRepository
import az.tribe.lifeplanner.testutil.FakeGamificationRepository
import az.tribe.lifeplanner.testutil.FakeHabitRepository
import az.tribe.lifeplanner.testutil.testHabit
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import az.tribe.lifeplanner.usecases.habit.CreditHabitsFromSessionUseCase
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class CreditHabitsFromSessionUseCaseTest {

    private lateinit var habits: FakeHabitRepository
    private lateinit var gamification: FakeGamificationRepository
    private lateinit var useCase: CreditHabitsFromSessionUseCase

    private val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    @BeforeTest
    fun setUp() {
        habits = FakeHabitRepository()
        gamification = FakeGamificationRepository()
        useCase = CreditHabitsFromSessionUseCase(
            habitRepository = habits,
            awardHabitCompletion = AwardHabitCompletionUseCase(
                habitRepository = habits,
                gamificationRepository = gamification,
                awardAbilityXpUseCase = AwardAbilityXpUseCase(FakeAbilityRepository()),
                settings = MapSettings(),
            ),
        )
    }

    @Test
    fun `a breathing session counts as one toward a three-a-day habit`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(
                    id = "breath",
                    title = "Take a breath",
                    targetCount = 3,
                    unit = "times",
                    completionSource = HabitCompletionSource.BREATHING,
                )
            )
        )

        val first = useCase(HabitCompletionSource.BREATHING).single()
        assertEquals(1, first.newCount)
        assertFalse(first.completed)

        useCase(HabitCompletionSource.BREATHING)
        val third = useCase(HabitCompletionSource.BREATHING).single()
        assertEquals(3, third.newCount)
        assertTrue(third.completed, "three sessions should complete a target of three")
    }

    @Test
    fun `a focus session fills a minutes habit by the minutes it lasted`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(
                    id = "meditate",
                    title = "Meditate 10min",
                    targetCount = 10,
                    unit = "min",
                    completionSource = HabitCompletionSource.FOCUS,
                )
            )
        )

        val credit = useCase(HabitCompletionSource.FOCUS, minutes = 25).single()

        assertEquals(25, credit.newCount)
        assertTrue(credit.completed, "25 minutes covers a 10 minute target in one session")
    }

    @Test
    fun `a short focus session only makes partial progress`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(
                    id = "meditate",
                    title = "Meditate 30min",
                    targetCount = 30,
                    unit = "min",
                    completionSource = HabitCompletionSource.FOCUS,
                )
            )
        )

        val credit = useCase(HabitCompletionSource.FOCUS, minutes = 10).single()

        assertEquals(10, credit.newCount)
        assertFalse(credit.completed)
    }

    @Test
    fun `habits linked to a different feature are left alone`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(id = "breath", completionSource = HabitCompletionSource.BREATHING),
                testHabit(id = "focus", completionSource = HabitCompletionSource.FOCUS),
            )
        )

        val credits = useCase(HabitCompletionSource.BREATHING)

        assertEquals(listOf("breath"), credits.map { it.habit.id })
        assertNull(habits.getCheckInByHabitAndDate("focus", today))
    }

    @Test
    fun `manual habits are never credited by a session`() = runTest {
        habits.setHabits(listOf(testHabit(id = "manual")))

        assertEquals(emptyList(), useCase(HabitCompletionSource.BREATHING))
        assertEquals(emptyList(), useCase(HabitCompletionSource.MANUAL))
        assertNull(habits.getCheckInByHabitAndDate("manual", today))
    }

    @Test
    fun `inactive habits are not credited`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(
                    id = "old",
                    isActive = false,
                    completionSource = HabitCompletionSource.BREATHING,
                )
            )
        )

        assertEquals(emptyList(), useCase(HabitCompletionSource.BREATHING))
    }

    @Test
    fun `XP is awarded once, on the session that completes the habit`() = runTest {
        habits.setHabits(
            listOf(
                testHabit(
                    id = "breath",
                    targetCount = 2,
                    unit = "times",
                    completionSource = HabitCompletionSource.BREATHING,
                )
            )
        )

        useCase(HabitCompletionSource.BREATHING)
        assertEquals(emptyList(), gamification.xpAwards, "no XP before the target is reached")

        val completing = useCase(HabitCompletionSource.BREATHING).single()
        assertTrue(completing.completed)
        assertTrue(
            gamification.xpAwards.contains(XpRewards.HABIT_CHECK_IN.toLong()),
            "the completing session should pay the check-in XP"
        )
        assertTrue(completing.xpAwarded >= XpRewards.HABIT_CHECK_IN)
    }

    @Test
    fun `an already completed habit is not credited again the same day`() = runTest {
        habits.setHabits(
            listOf(testHabit(id = "breath", completionSource = HabitCompletionSource.BREATHING))
        )

        val first = useCase(HabitCompletionSource.BREATHING).single()
        assertTrue(first.completed)
        val awardsAfterFirst = gamification.xpAwards.size

        assertEquals(emptyList(), useCase(HabitCompletionSource.BREATHING))
        assertEquals(awardsAfterFirst, gamification.xpAwards.size, "no second payout for the same day")
    }
}
