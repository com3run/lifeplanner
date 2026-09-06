package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.calendar.CalendarPreferences
import az.tribe.lifeplanner.data.calendar.CalendarReader
import az.tribe.lifeplanner.ui.calendar.CalendarViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calendarDataModule = module {
    singleOf(::CalendarReader)
    singleOf(::CalendarPreferences)
}

val calendarPresentationModule = module {
    viewModelOf(::CalendarViewModel)
}
