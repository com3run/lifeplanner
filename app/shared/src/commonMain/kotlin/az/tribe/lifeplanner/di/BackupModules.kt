package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightBackupRepository
import az.tribe.lifeplanner.domain.repository.BackupRepository
import az.tribe.lifeplanner.ui.backup.BackupViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val backupDataModule = module {
    singleOf(::SqlDelightBackupRepository) { bind<BackupRepository>() }
}

val backupPresentationModule = module {
    viewModelOf(::BackupViewModel)
}
