package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightFocusRepository
import az.tribe.lifeplanner.domain.repository.FocusRepository
import az.tribe.lifeplanner.ui.focus.FocusViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val focusDataModule = module {
    singleOf(::SqlDelightFocusRepository) { bind<FocusRepository>() }
}

val focusPresentationModule = module {
    viewModelOf(::FocusViewModel)
}
