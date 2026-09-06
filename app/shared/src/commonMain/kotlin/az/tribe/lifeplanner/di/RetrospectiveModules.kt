package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightBeginnerObjectiveRepository
import az.tribe.lifeplanner.data.repository.SqlDelightRetrospectiveRepository
import az.tribe.lifeplanner.domain.repository.BeginnerObjectiveRepository
import az.tribe.lifeplanner.domain.repository.RetrospectiveRepository
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import az.tribe.lifeplanner.ui.retrospective.RetrospectiveViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val retrospectiveDataModule = module {
    singleOf(::SqlDelightRetrospectiveRepository) { bind<RetrospectiveRepository>() }
    singleOf(::SqlDelightBeginnerObjectiveRepository) { bind<BeginnerObjectiveRepository>() }
}

val retrospectivePresentationModule = module {
    viewModelOf(::RetrospectiveViewModel)
    viewModelOf(::BeginnerObjectiveViewModel)
}
