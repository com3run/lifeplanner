package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightHabitRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.ui.habit.HabitChatViewModel
import az.tribe.lifeplanner.ui.habit.HabitDetailViewModel
import az.tribe.lifeplanner.ui.habit.HabitPracticeViewModel
import az.tribe.lifeplanner.ui.habit.HabitViewModel
import az.tribe.lifeplanner.ui.habit.SmartHabitGeneratorViewModel
import az.tribe.lifeplanner.usecases.habit.AwardHabitCompletionUseCase
import az.tribe.lifeplanner.usecases.habit.BackfillHabitTargetsUseCase
import az.tribe.lifeplanner.usecases.habit.CheckInHabitUseCase
import az.tribe.lifeplanner.usecases.habit.CreateHabitUseCase
import az.tribe.lifeplanner.usecases.habit.CreditHabitsFromSessionUseCase
import az.tribe.lifeplanner.usecases.habit.DeleteHabitUseCase
import az.tribe.lifeplanner.usecases.habit.GetAllHabitsUseCase
import az.tribe.lifeplanner.usecases.habit.GetHabitsByGoalUseCase
import az.tribe.lifeplanner.usecases.habit.GetHabitsWithTodayStatusUseCase
import az.tribe.lifeplanner.usecases.habit.RecommendLessonsForHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UncheckHabitUseCase
import az.tribe.lifeplanner.usecases.habit.UpdateHabitUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val habitDataModule = module {
    singleOf(::SqlDelightHabitRepository) { bind<HabitRepository>() }
}

val habitDomainModule = module {
    factoryOf(::GetAllHabitsUseCase)
    factoryOf(::CreateHabitUseCase)
    factoryOf(::BackfillHabitTargetsUseCase)
    factoryOf(::UpdateHabitUseCase)
    factoryOf(::DeleteHabitUseCase)
    factoryOf(::CheckInHabitUseCase)
    factoryOf(::AwardHabitCompletionUseCase)
    factoryOf(::CreditHabitsFromSessionUseCase)
    factoryOf(::RecommendLessonsForHabitUseCase)
    factoryOf(::UncheckHabitUseCase)
    factoryOf(::GetHabitsWithTodayStatusUseCase)
    factoryOf(::GetHabitsByGoalUseCase)
}

val habitPresentationModule = module {
    viewModelOf(::HabitViewModel)
    viewModelOf(::SmartHabitGeneratorViewModel)
    viewModelOf(::HabitChatViewModel)
    // Route arguments arrive through parametersOf, so these two stay explicit.
    viewModel { params -> HabitDetailViewModel(params.get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> HabitPracticeViewModel(params.get(), get(), get()) }
}
