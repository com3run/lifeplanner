package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.DerivedLifeBalanceRepository
import az.tribe.lifeplanner.data.repository.PredictiveWheelRepository
import az.tribe.lifeplanner.data.trajectory.BalancePastReconstructor
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.repository.LifeBalanceRepository
import az.tribe.lifeplanner.domain.repository.WheelRepository
import az.tribe.lifeplanner.ui.trajectory.TrajectoryViewModel
import az.tribe.lifeplanner.ui.wheel.WheelViewModel
import kotlinx.coroutines.flow.first
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

/** The life-balance wheel, its history and the trajectory built from it. */
val balanceDataModule = module {
    single<WheelRepository> {
        PredictiveWheelRepository(
            db = get(),
            goalRepository = get(),
            habitRepository = get(),
            journalRepository = get(),
            healthRepository = get(),
            abilityRepository = get(),
            // Read count comes through the repository's flow rather than a new query, so the
            // Learn hub stays the only thing that knows how reads are stored.
            knowledgeReadCount = { get<KnowledgeRepository>().readIds().first().size },
            syncManager = get(),
        )
    }
    singleOf(::DerivedLifeBalanceRepository) { bind<LifeBalanceRepository>() }
    singleOf(::BalancePastReconstructor)
}

val balancePresentationModule = module {
    viewModelOf(::WheelViewModel)
    viewModelOf(::TrajectoryViewModel)
}
