package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.domain.service.LocalPossibilityFallback
import az.tribe.lifeplanner.domain.service.PossibilityContextProvider
import az.tribe.lifeplanner.domain.service.PossibilityEngine
import az.tribe.lifeplanner.ui.home.PossibilityViewModel
import az.tribe.lifeplanner.ui.possibility.PossibilityModeViewModel
import az.tribe.lifeplanner.usecases.GeneratePossibilitiesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val possibilityDomainModule = module {
    single { PossibilityEngine() }
    singleOf(::PossibilityContextProvider)
    singleOf(::LocalPossibilityFallback)
    singleOf(::GeneratePossibilitiesUseCase)
}

val possibilityPresentationModule = module {
    viewModelOf(::PossibilityViewModel)
    viewModel { params -> PossibilityModeViewModel(params.get(), get(), get(), get(), get()) }
}
