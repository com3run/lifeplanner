package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.network.GeminiService
import az.tribe.lifeplanner.data.network.ProxiedGeminiService
import az.tribe.lifeplanner.data.repository.ProxiedGeminiRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalDependencyRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalHistoryRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGoalRepository
import az.tribe.lifeplanner.domain.repository.GeminiRepository
import az.tribe.lifeplanner.domain.repository.GoalDependencyRepository
import az.tribe.lifeplanner.domain.repository.GoalHistoryRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.goals.GoalsViewModel
import az.tribe.lifeplanner.usecases.AddMilestoneUseCase
import az.tribe.lifeplanner.usecases.ArchiveGoalUseCase
import az.tribe.lifeplanner.usecases.CalculateGoalCompletionRateUseCase
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
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val goalDataModule = module {
    singleOf(::SqlDelightGoalRepository) { bind<GoalRepository>() }
    singleOf(::SqlDelightGoalHistoryRepository) { bind<GoalHistoryRepository>() }
    singleOf(::SqlDelightGoalDependencyRepository) { bind<GoalDependencyRepository>() }
    singleOf(::ProxiedGeminiService) { bind<GeminiService>() }
    singleOf(::ProxiedGeminiRepository) { bind<GeminiRepository>() }
}

val goalDomainModule = module {
    factoryOf(::GetAllGoalsUseCase)
    factoryOf(::GetGoalsByTimelineUseCase)
    factoryOf(::GetGoalsByCategoryUseCase)
    factoryOf(::CreateGoalUseCase)
    factoryOf(::DeleteGoalUseCase)
    factoryOf(::UpdateGoalUseCase)
    factoryOf(::UpdateGoalProgressUseCase)
    factoryOf(::LogGoalChangeUseCase)
    factoryOf(::GetGoalHistoryUseCase)
    factoryOf(::GetGoalAnalyticsUseCase)
    factoryOf(::SearchGoalsUseCase)
    factoryOf(::GetActiveGoalsUseCase)
    factoryOf(::GetCompletedGoalsUseCase)
    factoryOf(::GetUpcomingDeadlinesUseCase)
    factoryOf(::UpdateGoalStatusUseCase)
    factoryOf(::UpdateGoalNotesUseCase)
    factoryOf(::ArchiveGoalUseCase)
    factoryOf(::UnarchiveGoalUseCase)
    factoryOf(::AddMilestoneUseCase)
    factoryOf(::UpdateMilestoneUseCase)
    factoryOf(::DeleteMilestoneUseCase)
    factoryOf(::ToggleMilestoneCompletionUseCase)
    factoryOf(::GetGoalByIdUseCase)
    factoryOf(::FilterGoalsByStatusUseCase)
    factoryOf(::CalculateGoalCompletionRateUseCase)
    factoryOf(::GetGoalStatisticsUseCase)
    factoryOf(::GenerateAiQuestionnaireUseCase)
    factoryOf(::GenerateAiGoalsUseCase)
}

val goalPresentationModule = module {
    viewModelOf(::GoalViewModel)
    viewModelOf(::GoalDependencyViewModel)
    viewModelOf(::GoalsViewModel)
}
