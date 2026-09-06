package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.auth.AuthService
import az.tribe.lifeplanner.data.auth.SupabaseAuthService
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.data.network.EdgeFunctionAiProxyService
import az.tribe.lifeplanner.data.network.AuthTokenProvider
import az.tribe.lifeplanner.data.network.BuiltinCoachFetcher
import az.tribe.lifeplanner.data.network.PersonaApiFetcher
import az.tribe.lifeplanner.data.network.SystemPromptFetcher
import az.tribe.lifeplanner.data.network.GeminiService
import az.tribe.lifeplanner.data.network.ProxiedGeminiService
import az.tribe.lifeplanner.data.repository.SupabaseAiUsageRepository
import az.tribe.lifeplanner.data.repository.SqlDelightBackupRepository
import az.tribe.lifeplanner.data.repository.SupabaseStoryRepository
import az.tribe.lifeplanner.data.repository.SqlDelightBeginnerObjectiveRepository
import az.tribe.lifeplanner.data.repository.SqlDelightChatRepository
import az.tribe.lifeplanner.data.repository.SupabaseCoachPostRepository
import az.tribe.lifeplanner.data.repository.SqlDelightCoachRepository
import az.tribe.lifeplanner.di.FileSharer
import az.tribe.lifeplanner.di.createFileSharer
import az.tribe.lifeplanner.data.repository.SqlDelightGamificationRepository
import az.tribe.lifeplanner.data.repository.ProxiedGeminiRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalDependencyRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalHistoryRepository
import az.tribe.lifeplanner.data.repository.SqlDelightFocusRepository
import az.tribe.lifeplanner.data.repository.SqlDelightRetrospectiveRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalRepository
import az.tribe.lifeplanner.data.repository.SqlDelightKnowledgeRepository
import az.tribe.lifeplanner.data.repository.PredictiveWheelRepository
import az.tribe.lifeplanner.ui.wheel.WheelViewModel
import kotlinx.coroutines.flow.first
import az.tribe.lifeplanner.data.repository.SqlDelightLifeValueRepository
import az.tribe.lifeplanner.data.repository.SqlDelightDecisionRepository
import az.tribe.lifeplanner.data.repository.SqlDelightDecisionProfileRepository
import az.tribe.lifeplanner.data.repository.SqlDelightIdentityStatementRepository
import az.tribe.lifeplanner.data.repository.SqlDelightAbilityRepository
import az.tribe.lifeplanner.data.repository.CoachOrchestrator
import az.tribe.lifeplanner.data.repository.SqlDelightUserSituationRepository
import az.tribe.lifeplanner.data.repository.SqlDelightHealthRepository
import az.tribe.lifeplanner.data.behavior.BehaviorTracker
import az.tribe.lifeplanner.data.repository.SqlDelightBehaviorRepository
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import az.tribe.lifeplanner.ui.screentime.ScreenTimeInsightViewModel
import az.tribe.lifeplanner.data.repository.SqlDelightHabitRepository
import az.tribe.lifeplanner.data.repository.SqlDelightJournalRepository
import az.tribe.lifeplanner.data.repository.DerivedLifeBalanceRepository
import az.tribe.lifeplanner.data.repository.SqlDelightReminderRepository
import az.tribe.lifeplanner.data.review.ReviewMessageBuilder
import az.tribe.lifeplanner.domain.service.SmartReminderManager
import az.tribe.lifeplanner.data.repository.SqlDelightUserRepository
import az.tribe.lifeplanner.domain.repository.AiUsageRepository
import az.tribe.lifeplanner.domain.repository.BackupRepository
import az.tribe.lifeplanner.domain.repository.StoryRepository
import az.tribe.lifeplanner.domain.repository.BeginnerObjectiveRepository
import az.tribe.lifeplanner.domain.repository.ChatRepository
import az.tribe.lifeplanner.domain.repository.CoachPostRepository
import az.tribe.lifeplanner.domain.repository.CoachRepository
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GeminiRepository
import az.tribe.lifeplanner.domain.repository.GoalDependencyRepository
import az.tribe.lifeplanner.domain.repository.GoalHistoryRepository
import az.tribe.lifeplanner.domain.repository.FocusRepository
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.repository.WheelRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import az.tribe.lifeplanner.domain.repository.IdentityStatementRepository
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import az.tribe.lifeplanner.domain.repository.ReminderRepository
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.notification.NotificationSchedulerInterface
import az.tribe.lifeplanner.notification.getNotificationScheduler
import az.tribe.lifeplanner.util.NetworkConnectivityObserver
import az.tribe.lifeplanner.widget.WidgetDataSyncService
import az.tribe.lifeplanner.data.calendar.CalendarReader
import az.tribe.lifeplanner.data.health.HealthDataManager
import az.tribe.lifeplanner.ui.ability.AbilityDetailViewModel
import az.tribe.lifeplanner.ui.ability.AbilityViewModel
import az.tribe.lifeplanner.ui.calendar.CalendarViewModel
import az.tribe.lifeplanner.ui.health.HealthViewModel
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.chat.ChatViewModel
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.habit.HabitViewModel
import az.tribe.lifeplanner.ui.journal.JournalViewModel
import az.tribe.lifeplanner.ui.backup.BackupViewModel
import az.tribe.lifeplanner.ui.focus.FocusViewModel
import az.tribe.lifeplanner.ui.retrospective.RetrospectiveViewModel
import az.tribe.lifeplanner.ui.coach.CoachViewModel
import az.tribe.lifeplanner.ui.onboarding.AboutYouViewModel
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingViewModel
import az.tribe.lifeplanner.ui.reminder.ReminderViewModel
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.habit.SmartHabitGeneratorViewModel
import az.tribe.lifeplanner.ui.habit.HabitChatViewModel
import az.tribe.lifeplanner.ui.components.WeeklyEngagementViewModel
import az.tribe.lifeplanner.ui.profile.YouViewModel
import az.tribe.lifeplanner.ui.search.SearchViewModel
import az.tribe.lifeplanner.usecases.journal.CreateJournalEntryUseCase
import az.tribe.lifeplanner.usecases.journal.DeleteJournalEntryUseCase
import az.tribe.lifeplanner.usecases.journal.GetAllJournalEntriesUseCase
import az.tribe.lifeplanner.usecases.journal.GetJournalEntriesByGoalUseCase
import az.tribe.lifeplanner.usecases.journal.GetRecentJournalEntriesUseCase
import az.tribe.lifeplanner.usecases.journal.UpdateJournalEntryUseCase
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import az.tribe.lifeplanner.usecases.health.AutoCompleteHealthHabitsUseCase
import az.tribe.lifeplanner.usecases.health.SyncHealthDataUseCase
import az.tribe.lifeplanner.usecases.habit.BackfillHabitTargetsUseCase
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.CreditHabitsFromSessionUseCase
import az.tribe.lifeplanner.usecases.habit.RecommendLessonsForHabitUseCase
import az.tribe.lifeplanner.usecases.habit.CreateHabitUseCase
import az.tribe.lifeplanner.usecases.habit.DeleteHabitUseCase
import az.tribe.lifeplanner.usecases.habit.GetAllHabitsUseCase
import az.tribe.lifeplanner.usecases.habit.GetHabitsByGoalUseCase
import az.tribe.lifeplanner.usecases.habit.GetHabitsWithTodayStatusUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UpdateHabitUseCase
import az.tribe.lifeplanner.usecases.AddMilestoneUseCase
import az.tribe.lifeplanner.usecases.ArchiveGoalUseCase
import az.tribe.lifeplanner.usecases.CalculateGoalCompletionRateUseCase
import az.tribe.lifeplanner.usecases.AutoLinkGoalValuesUseCase
import az.tribe.lifeplanner.usecases.CreateGoalUseCase
import az.tribe.lifeplanner.usecases.DeleteGoalUseCase
import az.tribe.lifeplanner.usecases.DeleteMilestoneUseCase
import az.tribe.lifeplanner.usecases.FilterGoalsByStatusUseCase
import az.tribe.lifeplanner.usecases.GenerateAiGoalsUseCase
import az.tribe.lifeplanner.usecases.GenerateAiQuestionnaireUseCase
import az.tribe.lifeplanner.usecases.GetActiveGoalsUseCase
import az.tribe.lifeplanner.usecases.GetAllGoalsUseCase
import az.tribe.lifeplanner.usecases.GetCompletedGoalsUseCase
import az.tribe.lifeplanner.usecases.GetGoalAnalyticsUseCase
import az.tribe.lifeplanner.usecases.GetGoalByIdUseCase
import az.tribe.lifeplanner.usecases.GetGoalHistoryUseCase
import az.tribe.lifeplanner.usecases.GetGoalStatisticsUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByCategoryUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByTimelineUseCase
import az.tribe.lifeplanner.usecases.GetUpcomingDeadlinesUseCase
import az.tribe.lifeplanner.usecases.LogGoalChangeUseCase
import az.tribe.lifeplanner.usecases.SearchGoalsUseCase
import az.tribe.lifeplanner.usecases.ToggleMilestoneCompletionUseCase
import az.tribe.lifeplanner.usecases.UnarchiveGoalUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalNotesUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalProgressUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalStatusUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalUseCase
import az.tribe.lifeplanner.usecases.UpdateMilestoneUseCase
import com.russhwolf.settings.Settings
import az.tribe.lifeplanner.ui.theme.ThemeController
import io.github.jan.supabase.auth.auth
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

const val DB_NAME = "LifePlannerDB.db"

val appModule = module {

    single { DatabaseDriverFactory() }
    single { az.tribe.lifeplanner.location.LocationProvider() }
    single { az.tribe.lifeplanner.data.trajectory.BalancePastReconstructor(get()) }
    single<az.tribe.lifeplanner.domain.repository.WeatherRepository> {
        az.tribe.lifeplanner.data.network.OpenMeteoWeatherRepository(get(), get())
    }
    single { SharedDatabase(get()) }
    single { Settings() }
    single { ThemeController(get()) }
    single<FileSharer> { createFileSharer() }
    single { WidgetDataSyncService() }
    single { NetworkConnectivityObserver() }
    single<NotificationSchedulerInterface> { getNotificationScheduler() }

    // Auth Service (Supabase, multiplatform, no platform-specific needed)
    single<AuthService> { SupabaseAuthService(get()) }

    // Auth token provider (Supabase session → JWT, with auto-refresh)
    // Uses a Mutex to prevent concurrent refresh attempts from racing.
    single<AuthTokenProvider> {
        val supabase: io.github.jan.supabase.SupabaseClient = get()
        val refreshMutex = kotlinx.coroutines.sync.Mutex()
        AuthTokenProvider {
            // Try current session first
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                // Check if the access token is expired or about to expire (within 30s)
                val now = kotlinx.datetime.Clock.System.now()
                val timeUntilExpiry = session.expiresAt - now
                if (timeUntilExpiry.inWholeSeconds <= 30) {
                    // Serialize refresh attempts, if another call already refreshed, reuse it
                    refreshMutex.lock()
                    try {
                        // Re-check after acquiring lock, another call may have refreshed already
                        val currentSession = supabase.auth.currentSessionOrNull()
                        if (currentSession != null) {
                            val freshExpiry = currentSession.expiresAt - kotlinx.datetime.Clock.System.now()
                            if (freshExpiry.inWholeSeconds > 30) {
                                return@AuthTokenProvider currentSession.accessToken
                            }
                        }
                        // Still expired, refresh with retry
                        var lastException: Exception? = null
                        for (attempt in 1..3) {
                            try {
                                supabase.auth.refreshCurrentSession()
                                val refreshed = supabase.auth.currentSessionOrNull()?.accessToken
                                if (refreshed != null) return@AuthTokenProvider refreshed
                            } catch (e: Exception) {
                                lastException = e
                                co.touchlab.kermit.Logger.w("AuthTokenProvider") {
                                    "Token refresh attempt $attempt failed: ${e.message}"
                                }
                                if (attempt < 3) kotlinx.coroutines.delay(500L * attempt)
                            }
                        }
                        co.touchlab.kermit.Logger.e("AuthTokenProvider") {
                            "Token refresh failed after 3 attempts: ${lastException?.message}"
                        }
                        throw IllegalStateException("Authentication expired. Please sign in again.")
                    } finally {
                        refreshMutex.unlock()
                    }
                } else {
                    session.accessToken
                }
            } else {
                // No session at all. This is the guest case, and it is recoverable.
                //
                // signInAsGuest() is meant to create a real Supabase *anonymous* session, which
                // carries a JWT the ai-proxy accepts, so guests are entitled to AI. But that call
                // has a 10s timeout, and when it expires (flaky network on first launch) the app
                // silently falls back to a local-only guest with no session. Previously that state
                // was permanent for the whole install: every AI call threw here, before reaching
                // the network, so the coach produced nothing and onboarding seeded no goals.
                //
                // Heal it instead: establish the anonymous session on demand. Mutex-guarded so
                // concurrent AI calls do not each start their own sign-in.
                refreshMutex.lock()
                try {
                    supabase.auth.currentSessionOrNull()?.let { existing ->
                        return@AuthTokenProvider existing.accessToken
                    }
                    co.touchlab.kermit.Logger.i("AuthTokenProvider") {
                        "No session; establishing an anonymous one so this guest can use AI"
                    }
                    supabase.auth.signInAnonymously()
                    supabase.auth.currentSessionOrNull()?.accessToken
                        ?: throw IllegalStateException("Not authenticated. Please sign in.")
                } catch (e: IllegalStateException) {
                    throw e
                } catch (e: Exception) {
                    // Offline, or anonymous sign-ups disabled on the project. Genuinely cannot
                    // reach AI, so surface it as an auth problem rather than a silent nothing.
                    co.touchlab.kermit.Logger.w("AuthTokenProvider") {
                        "Anonymous sign-in for guest failed: ${e.message}"
                    }
                    throw IllegalStateException("Not authenticated. Please sign in.")
                } finally {
                    refreshMutex.unlock()
                }
            }
        }
    }

    // AI Proxy Service
    single<AiProxyService> { EdgeFunctionAiProxyService(get(), get(), get()) }
    single { BuiltinCoachFetcher(get()) }
    single { PersonaApiFetcher(get(), get()) }
    single { SystemPromptFetcher(get()) }
    single { az.tribe.lifeplanner.data.network.KnowledgeFetcher(get(), get()) }

    // Repositories
    single<GeminiService> { ProxiedGeminiService(get<AiProxyService>()) }
    single<GeminiRepository> { ProxiedGeminiRepository(get()) }

    single<GoalRepository> { SqlDelightGoalRepository(get(), get(), get()) }
    single<LifeValueRepository> { SqlDelightLifeValueRepository(get(), get()) }
    single<KnowledgeRepository> { SqlDelightKnowledgeRepository(get(), get()) }
    single<WheelRepository> {
        PredictiveWheelRepository(
            db = get(),
            goalRepository = get(),
            habitRepository = get(),
            journalRepository = get(),
            healthRepository = get(),
            abilityRepository = get(),
            // Read count comes through the repository's flow rather than a new query, so the
            // Learn hub stays the only thing that knows how reads are stored.
            knowledgeReadCount = { get<KnowledgeRepository>().readIds().first().size },
            syncManager = get(),
        )
    }
    single<DecisionRepository> { SqlDelightDecisionRepository(get(), get()) }
    single<IdentityStatementRepository> { SqlDelightIdentityStatementRepository(get(), get()) }
    single<DecisionProfileRepository> { SqlDelightDecisionProfileRepository(get(), get()) }
    single<GoalHistoryRepository> { SqlDelightGoalHistoryRepository(get(), get()) }
    single<GamificationRepository> { SqlDelightGamificationRepository(get(), get(), get(), get()) }
    single<UserRepository> { SqlDelightUserRepository(get(), get()) }
    single<HabitRepository> { SqlDelightHabitRepository(get(), get(), get()) }
    single<JournalRepository> { SqlDelightJournalRepository(get(), get()) }
    single<GoalDependencyRepository> { SqlDelightGoalDependencyRepository(get(), get()) }
    single<CoachRepository> { SqlDelightCoachRepository(get(), get()) }
    single<CoachPostRepository> { SupabaseCoachPostRepository(get()) }
    single { az.tribe.lifeplanner.domain.service.ChoicePointDetector() }
    single { az.tribe.lifeplanner.domain.service.CausalInsightEngine() }
    single { az.tribe.lifeplanner.domain.service.CausalInsightProvider(get(), get(), get(), get(), get()) }
    single { az.tribe.lifeplanner.domain.service.CalibrationEngine() }
    single { az.tribe.lifeplanner.domain.service.CalibrationProvider(get(), get(), get()) }
    single<az.tribe.lifeplanner.core.PremiumGate> { az.tribe.lifeplanner.core.DefaultPremiumGate() }
    single { CoachOrchestrator() }
    single<ChatRepository> { SqlDelightChatRepository(get(), get<AiProxyService>(), get(), get(), get(), get()) }
    single { ReviewMessageBuilder(get()) }
    single<ReminderRepository> { SqlDelightReminderRepository(get(), get(), get()) }
    single { SmartReminderManager(get()) }
    single { az.tribe.lifeplanner.domain.service.PossibilityEngine() }
    single { az.tribe.lifeplanner.domain.service.PossibilityContextProvider(get(), get(), get(), get()) }
    single<FocusRepository> { SqlDelightFocusRepository(get(), get()) }
    single<AiUsageRepository> { SupabaseAiUsageRepository(get()) }
    single<RetrospectiveRepository> { SqlDelightRetrospectiveRepository(get()) }
    single<BeginnerObjectiveRepository> { SqlDelightBeginnerObjectiveRepository(get(), get()) }
    single<AbilityRepository> { SqlDelightAbilityRepository(get()) }
    single<UserSituationRepository> { SqlDelightUserSituationRepository(get(), get()) }
    single { HealthDataManager() }
    single<HealthRepository> { SqlDelightHealthRepository(get(), get(), get()) }
    single { CalendarReader() }
    single { az.tribe.lifeplanner.data.calendar.CalendarPreferences(get()) }

    // Behavior tracking
    single<BehaviorRepository> { SqlDelightBehaviorRepository(get()) }
    single { BehaviorTracker(get()) }

    // Existing Use Cases
    factory { GetAllGoalsUseCase(get()) }
    factory { az.tribe.lifeplanner.usecases.ComputeValueAlignmentUseCase(get(), get(), get()) }
    factory { GetGoalsByTimelineUseCase(get()) }
    factory { GetGoalsByCategoryUseCase(get()) }
    factory { CreateGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { az.tribe.lifeplanner.usecases.PromoteTopValuesToLifeValuesUseCase(get(), get(), get()) }
    factory { az.tribe.lifeplanner.usecases.SeedDefaultLifeValuesUseCase(get(), get()) }
    factory { UpdateGoalUseCase(get()) }
    factory { AutoLinkGoalValuesUseCase(get(), get()) }
    factory { UpdateGoalProgressUseCase(get()) }
    factory { LogGoalChangeUseCase(get()) }
    factory { GetGoalHistoryUseCase(get()) }
    factory { GetGoalAnalyticsUseCase(get()) }

    // New Search and Filter Use Cases
    factory { SearchGoalsUseCase(get()) }
    factory { GetActiveGoalsUseCase(get()) }
    factory { GetCompletedGoalsUseCase(get()) }
    factory { GetUpcomingDeadlinesUseCase(get()) }

    // Goal Management Use Cases
    factory { UpdateGoalStatusUseCase(get()) }
    factory { UpdateGoalNotesUseCase(get()) }
    factory { ArchiveGoalUseCase(get()) }
    factory { UnarchiveGoalUseCase(get()) }

    // Milestone Management Use Cases
    factory { AddMilestoneUseCase(get()) }
    factory { UpdateMilestoneUseCase(get()) }
    factory { DeleteMilestoneUseCase(get()) }
    factory { ToggleMilestoneCompletionUseCase(get()) }

    // Utility Use Cases
    factory { GetGoalByIdUseCase(get()) }
    factory { FilterGoalsByStatusUseCase(get()) }
    factory { CalculateGoalCompletionRateUseCase() }
    factory { GetGoalStatisticsUseCase(get()) }

    factory { GenerateAiQuestionnaireUseCase(get()) }
    factory { GenerateAiGoalsUseCase(get()) }

    // Ability Use Cases
    factory { AwardAbilityXpUseCase(get()) }

    // Health Use Cases
    factory { AutoCompleteHealthHabitsUseCase(get(), get(), get()) }
    factory { az.tribe.lifeplanner.usecases.health.GetHealthHabitProgressUseCase(get(), get()) }
    factory { SyncHealthDataUseCase(get(), get()) }

    // Habit Use Cases
    factory { GetAllHabitsUseCase(get()) }
    factory { CreateHabitUseCase(get()) }
    factory { BackfillHabitTargetsUseCase(get(), get()) }
    factory { UpdateHabitUseCase(get()) }
    factory { DeleteHabitUseCase(get()) }
    factory { CheckInHabitUseCase(get()) }
    factory { AwardHabitCompletionUseCase(get(), get(), get(), get()) }
    factory { CreditHabitsFromSessionUseCase(get(), get()) }
    factory { RecommendLessonsForHabitUseCase(get(), get()) }
    factory { UncheckHabitUseCase(get()) }
    factory { GetHabitsWithTodayStatusUseCase(get()) }
    factory { GetHabitsByGoalUseCase(get()) }

    // Journal Use Cases
    factory { GetAllJournalEntriesUseCase(get()) }
    factory { CreateJournalEntryUseCase(get()) }
    factory { UpdateJournalEntryUseCase(get()) }
    factory { DeleteJournalEntryUseCase(get()) }
    factory { GetRecentJournalEntriesUseCase(get()) }
    factory { GetJournalEntriesByGoalUseCase(get()) }

    // Life Balance Repository
    single<LifeBalanceRepository> { DerivedLifeBalanceRepository(get(), get(), get<AiProxyService>(), get()) }

    // Backup Repository
    single<BackupRepository> { SqlDelightBackupRepository(get(), get()) }

    // Story Repository
    single<StoryRepository> { SupabaseStoryRepository(get()) }

    // ViewModels
    viewModelOf(::WheelViewModel)
    viewModelOf(::GoalViewModel)
    viewModelOf(::GamificationViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::HabitViewModel)
    viewModelOf(::JournalViewModel)
    viewModelOf(::GoalDependencyViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::CoachViewModel)
    viewModelOf(::ReminderViewModel)
    viewModel { az.tribe.lifeplanner.ui.decision.DecisionViewModel(get(), get(), get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.causal.CausalInsightsViewModel(get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.becoming.BecomingViewModel(get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.decision.MetacognitiveReviewViewModel(get()) }
    viewModel { az.tribe.lifeplanner.ui.wiring.WiringViewModel(get()) }
    viewModelOf(::BackupViewModel)
    viewModelOf(::FocusViewModel)
    viewModel { az.tribe.lifeplanner.ui.today.TodayViewModel(get(), get(), get(), get()) }
    single { az.tribe.lifeplanner.ui.foryou.HomeFeedBuilder(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single<az.tribe.lifeplanner.ui.intro.IntroSeenStore> { az.tribe.lifeplanner.ui.intro.SettingsIntroSeenStore() }
    viewModel { az.tribe.lifeplanner.ui.foryou.ForYouViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.today.TodayWeatherViewModel(get()) }
    viewModel { az.tribe.lifeplanner.ui.trajectory.TrajectoryViewModel(get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.foryou.LearnHubViewModel(get(), get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.foryou.KnowledgeDetailViewModel(get(), get()) }
    single { az.tribe.lifeplanner.usecases.GeneratePossibilitiesUseCase(get(), az.tribe.lifeplanner.domain.service.LocalPossibilityFallback()) }
    viewModel { params -> az.tribe.lifeplanner.ui.possibility.PossibilityModeViewModel(params.get(), get(), get(), get(), get()) }
    viewModel { az.tribe.lifeplanner.ui.goals.GoalsViewModel(get()) }
    viewModel { params -> az.tribe.lifeplanner.ui.habit.HabitDetailViewModel(params.get(), get(), get(), get(), get(), get(), get()) }
    viewModelOf(::RetrospectiveViewModel)
    viewModelOf(::BeginnerObjectiveViewModel)
    viewModelOf(::AbilityViewModel)
    viewModel { params -> AbilityDetailViewModel(params.get(), get(), get(), get(), get()) }
    viewModel { params -> az.tribe.lifeplanner.ui.habit.HabitPracticeViewModel(params.get(), get(), get()) }
    viewModelOf(::HealthViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::HomeViewModel)
    viewModel { az.tribe.lifeplanner.ui.home.PossibilityViewModel(get(), get()) }
    viewModelOf(::WeeklyEngagementViewModel)
    viewModelOf(::YouViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SmartHabitGeneratorViewModel)
    viewModelOf(::HabitChatViewModel)
    viewModelOf(::CoachOnboardingViewModel)
    viewModelOf(::AboutYouViewModel)
    viewModelOf(::ScreenTimeInsightViewModel)
}