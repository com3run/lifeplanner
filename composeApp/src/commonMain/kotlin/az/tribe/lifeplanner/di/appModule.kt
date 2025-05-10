package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.GoalRepositoryImpl
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.domain.GoalRepository
import az.tribe.lifeplanner.ui.GoalViewModel
import az.tribe.lifeplanner.usecases.CreateGoalUseCase
import az.tribe.lifeplanner.usecases.DeleteGoalUseCase
import az.tribe.lifeplanner.usecases.GetAllGoalsUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByCategoryUseCase
import az.tribe.lifeplanner.usecases.GetGoalsByTimelineUseCase
import az.tribe.lifeplanner.usecases.UpdateGoalUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

const val DB_NAME = "LifePlannerDB.db"

val appModule = module {

    single { DatabaseDriverFactory() }
    single { SharedDatabase(get()) }

    single<GoalRepository> { GoalRepositoryImpl(get()) }

    factory { GetAllGoalsUseCase(get()) }
    factory { GetGoalsByTimelineUseCase(get()) }
    factory { GetGoalsByCategoryUseCase(get()) }
    factory { CreateGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { UpdateGoalUseCase(get()) }

    viewModel {
        GoalViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}