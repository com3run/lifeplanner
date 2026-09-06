package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightJournalRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.ui.journal.JournalViewModel
import az.tribe.lifeplanner.usecases.journal.CreateJournalEntryUseCase
import az.tribe.lifeplanner.usecases.journal.DeleteJournalEntryUseCase
import az.tribe.lifeplanner.usecases.journal.GetAllJournalEntriesUseCase
import az.tribe.lifeplanner.usecases.journal.GetJournalEntriesByGoalUseCase
import az.tribe.lifeplanner.usecases.journal.GetRecentJournalEntriesUseCase
import az.tribe.lifeplanner.usecases.journal.UpdateJournalEntryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val journalDataModule = module {
    singleOf(::SqlDelightJournalRepository) { bind<JournalRepository>() }
}

val journalDomainModule = module {
    factoryOf(::GetAllJournalEntriesUseCase)
    factoryOf(::CreateJournalEntryUseCase)
    factoryOf(::UpdateJournalEntryUseCase)
    factoryOf(::DeleteJournalEntryUseCase)
    factoryOf(::GetRecentJournalEntriesUseCase)
    factoryOf(::GetJournalEntriesByGoalUseCase)
}

val journalPresentationModule = module {
    viewModelOf(::JournalViewModel)
}
