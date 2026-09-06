package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightReminderRepository
import az.tribe.lifeplanner.domain.repository.ReminderRepository
import az.tribe.lifeplanner.domain.service.SmartReminderManager
import az.tribe.lifeplanner.ui.reminder.ReminderViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val reminderDataModule = module {
    singleOf(::SqlDelightReminderRepository) { bind<ReminderRepository>() }
}

val reminderDomainModule = module {
    singleOf(::SmartReminderManager)
}

val reminderPresentationModule = module {
    viewModelOf(::ReminderViewModel)
}
