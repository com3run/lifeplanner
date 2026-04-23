package az.tribe.lifeplanner.ui.onboarding

import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.enum.AiProvider
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.ActivityLevel
import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.EmploymentStatus
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.OnboardingData
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.User
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import az.tribe.lifeplanner.testutil.FakeGoalRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoachOnboardingViewModelTest {

    private lateinit var settings: MapSettings
    private lateinit var goalRepository: FakeGoalRepository
    private lateinit var viewModel: CoachOnboardingViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settings = MapSettings()
        goalRepository = FakeGoalRepository()
        viewModel = createViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CoachOnboardingViewModel(
        userSituationRepository = FakeUserSituationRepository(),
        userRepository = FakeUserRepository(),
        goalRepository = goalRepository,
        aiProxyService = FakeAiProxyService(),
        settings = settings
    )

    // ── isComplete ────────────────────────────────────────────────────────────

    @Test
    fun `isComplete returns false when key is absent`() {
        assertFalse(CoachOnboardingViewModel.isComplete(settings))
    }

    @Test
    fun `isComplete returns true when key is set`() {
        settings.putBoolean(CoachOnboardingViewModel.COACH_ONBOARDING_KEY, true)
        assertTrue(CoachOnboardingViewModel.isComplete(settings))
    }

    // ── resetForNewSession ────────────────────────────────────────────────────

    @Test
    fun `resetForNewSession clears completion flag`() {
        settings.putBoolean(CoachOnboardingViewModel.COACH_ONBOARDING_KEY, true)
        viewModel.resetForNewSession()
        assertFalse(CoachOnboardingViewModel.isComplete(settings))
    }

    @Test
    fun `resetForNewSession resets phase to LUNA_INTRO`() {
        viewModel.advance() // -> LUNA_NAME
        viewModel.resetForNewSession()
        assertEquals(OnboardingPhase.LUNA_INTRO, viewModel.phase.value)
    }

    @Test
    fun `resetForNewSession clears all answer fields`() {
        viewModel.userName = "Alice"
        viewModel.userAge = 30
        viewModel.topPriority = GoalCategory.CAREER
        viewModel.mindDump = "I want to change jobs"
        viewModel.activityLevel = ActivityLevel.ACTIVE
        viewModel.employmentStatus = EmploymentStatus.EMPLOYED

        viewModel.resetForNewSession()

        assertEquals("", viewModel.userName)
        assertNull(viewModel.userAge)
        assertNull(viewModel.topPriority)
        assertEquals("", viewModel.mindDump)
        assertNull(viewModel.activityLevel)
        assertNull(viewModel.employmentStatus)
    }

    @Test
    fun `resetForNewSession clears persisted phase so fresh VM starts from LUNA_INTRO`() {
        viewModel.advance() // persists LUNA_NAME in settings
        viewModel.resetForNewSession()

        val freshVm = createViewModel()
        assertEquals(OnboardingPhase.LUNA_INTRO, freshVm.phase.value)
    }

    // ── advance / back ────────────────────────────────────────────────────────

    @Test
    fun `advance progresses through luna phases in order`() {
        assertEquals(OnboardingPhase.LUNA_INTRO, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.LUNA_NAME, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.LUNA_PRIORITY, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.LUNA_WELLBEING, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_INTRO, viewModel.phase.value)
    }

    @Test
    fun `back does not go before LUNA_INTRO`() {
        viewModel.back()
        assertEquals(OnboardingPhase.LUNA_INTRO, viewModel.phase.value)
    }

    @Test
    fun `back reverses advance`() {
        viewModel.advance() // -> LUNA_NAME
        viewModel.advance() // -> LUNA_PRIORITY
        viewModel.back()
        assertEquals(OnboardingPhase.LUNA_NAME, viewModel.phase.value)
    }

    @Test
    fun `career specialist path has 4 questions before MIND_DUMP`() {
        viewModel.topPriority = GoalCategory.CAREER
        repeat(5) { viewModel.advance() } // -> SPECIALIST_Q1
        assertEquals(OnboardingPhase.SPECIALIST_Q1, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_Q2, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_Q3, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_Q4, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.MIND_DUMP, viewModel.phase.value)
    }

    @Test
    fun `fitness specialist path has 3 questions before MIND_DUMP`() {
        viewModel.topPriority = GoalCategory.BODY
        repeat(5) { viewModel.advance() } // -> SPECIALIST_Q1
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_Q2, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.SPECIALIST_Q3, viewModel.phase.value)
        viewModel.advance(); assertEquals(OnboardingPhase.MIND_DUMP, viewModel.phase.value)
    }

    @Test
    fun `MIND_DUMP advances to COMPLETE`() {
        viewModel.topPriority = GoalCategory.BODY
        repeat(5) { viewModel.advance() }  // -> SPECIALIST_Q1
        repeat(3) { viewModel.advance() }  // Q1->Q2->Q3->MIND_DUMP
        viewModel.advance()
        assertEquals(OnboardingPhase.COMPLETE, viewModel.phase.value)
    }

    @Test
    fun `COMPLETE phase does not advance further`() {
        viewModel.topPriority = GoalCategory.BODY
        repeat(10) { viewModel.advance() } // exhaust all phases
        assertEquals(OnboardingPhase.COMPLETE, viewModel.phase.value)
        viewModel.advance()
        assertEquals(OnboardingPhase.COMPLETE, viewModel.phase.value)
    }

    // ── overallCompleteness ───────────────────────────────────────────────────

    @Test
    fun `overallCompleteness stays below 1 for a fresh session`() {
        assertTrue(viewModel.overallCompleteness() < 1f)
    }

    @Test
    fun `overallCompleteness increases as answers are filled`() {
        val before = viewModel.overallCompleteness()
        viewModel.userName = "Alice"
        viewModel.topPriority = GoalCategory.BODY
        assertTrue(viewModel.overallCompleteness() > before)
    }

    // ── completeOnboarding ────────────────────────────────────────────────────

    @Test
    fun `completeOnboarding marks isComplete in settings and invokes callback`() = runTest {
        var completed = false
        viewModel.completeOnboarding { completed = true }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(CoachOnboardingViewModel.isComplete(settings))
        assertTrue(completed)
    }

    @Test
    fun `completeOnboarding with mindDump inserts a goal`() = runTest {
        viewModel.mindDump = "I want to get fit and lose weight"
        viewModel.completeOnboarding {}
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(goalRepository.getAllGoals().isNotEmpty())
    }

    @Test
    fun `completeOnboarding without mindDump inserts no goal`() = runTest {
        viewModel.mindDump = ""
        viewModel.completeOnboarding {}
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(goalRepository.getAllGoals().isEmpty())
    }
}

// ─── Test fakes ───────────────────────────────────────────────────────────────

private class FakeUserSituationRepository : UserSituationRepository {
    override fun observe(): Flow<UserSituation?> = flowOf(null)
    override suspend fun getOrCreate(userId: String): UserSituation = UserSituation()
    override suspend fun upsert(userId: String, situation: UserSituation) {}
    override suspend fun updateMeta(userId: String, meta: MetaSlice) {}
    override suspend fun updateCareer(userId: String, career: CareerSlice) {}
    override suspend fun updateMoney(userId: String, money: MoneySlice) {}
    override suspend fun updateBody(userId: String, body: BodySlice) {}
    override suspend fun updatePeople(userId: String, people: PeopleSlice) {}
    override suspend fun updatePurpose(userId: String, purpose: PurposeSlice) {}
}

private class FakeUserRepository : UserRepository {
    override fun getCurrentUserFlow(): Flow<User?> = flowOf(null)
    override suspend fun getCurrentUser(): User? = null
    override suspend fun createUser(user: User) {}
    override suspend fun updateUser(user: User) {}
    override suspend fun saveOnboardingData(userId: String, onboardingData: OnboardingData) {}
    override suspend fun markOnboardingComplete(userId: String) {}
    override suspend fun hasCompletedOnboarding(): Boolean = false
    override suspend fun deleteUser(userId: String) {}
    override suspend fun deleteAllUsers() {}
    override suspend fun getUserByFirebaseUid(uid: String): User? = null
    override suspend fun clearAllLocalData() {}
}

private class FakeAiProxyService : AiProxyService {
    override suspend fun generateText(prompt: String, systemPrompt: String?, provider: AiProvider?): String = ""

    override suspend fun generateStructuredJson(
        prompt: String,
        responseSchema: JsonObject,
        systemPrompt: String?,
        provider: AiProvider?
    ): String = """{"title":"Get fit","description":"Build a fitness routine.","category":"BODY"}"""

    override suspend fun chat(
        messages: List<AiProxyService.ChatMessage>,
        systemPrompt: String?,
        responseSchema: JsonObject?,
        provider: AiProvider?
    ): String = ""

    override fun chatStream(
        messages: List<AiProxyService.ChatMessage>,
        systemPrompt: String?,
        provider: AiProvider?
    ) = kotlinx.coroutines.flow.emptyFlow()
}
