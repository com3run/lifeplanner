package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.ui.screentime.ScreenTimeInsightViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val screenTimePresentationModule = module {
    viewModelOf(::ScreenTimeInsightViewModel)
}
