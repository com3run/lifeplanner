package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightAbilityRepository
import az.tribe.lifeplanner.data.repository.SqlDelightGamificationRepository
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.ui.ability.AbilityDetailViewModel
import az.tribe.lifeplanner.ui.ability.AbilityViewModel
import az.tribe.lifeplanner.ui.components.WeeklyEngagementViewModel
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val gamificationDataModule = module {
    singleOf(::SqlDelightGamificationRepository) { bind<GamificationRepository>() }
    singleOf(::SqlDelightAbilityRepository) { bind<AbilityRepository>() }
}

val gamificationDomainModule = module {
    factoryOf(::AwardAbilityXpUseCase)
}

val gamificationPresentationModule = module {
    viewModelOf(::GamificationViewModel)
    viewModelOf(::AbilityViewModel)
    viewModelOf(::WeeklyEngagementViewModel)
    viewModel { params -> AbilityDetailViewModel(params.get(), get(), get(), get(), get()) }
}
