package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.health.HealthDataManager
import az.tribe.lifeplanner.data.repository.SqlDelightHealthRepository
import az.tribe.lifeplanner.domain.repository.HealthRepository
import az.tribe.lifeplanner.ui.health.HealthViewModel
import az.tribe.lifeplanner.usecases.health.AutoCompleteHealthHabitsUseCase
import az.tribe.lifeplanner.usecases.health.GetHealthHabitProgressUseCase
import az.tribe.lifeplanner.usecases.health.SyncHealthDataUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val healthDataModule = module {
    singleOf(::HealthDataManager)
    singleOf(::SqlDelightHealthRepository) { bind<HealthRepository>() }
}

val healthDomainModule = module {
    factoryOf(::AutoCompleteHealthHabitsUseCase)
    factoryOf(::GetHealthHabitProgressUseCase)
    factoryOf(::SyncHealthDataUseCase)
}

val healthPresentationModule = module {
    viewModelOf(::HealthViewModel)
}
