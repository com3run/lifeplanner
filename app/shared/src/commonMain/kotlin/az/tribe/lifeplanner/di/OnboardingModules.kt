package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.ui.onboarding.AboutYouViewModel
import az.tribe.lifeplanner.ui.onboarding.CoachOnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingPresentationModule = module {
    viewModelOf(::CoachOnboardingViewModel)
    viewModelOf(::AboutYouViewModel)
}
