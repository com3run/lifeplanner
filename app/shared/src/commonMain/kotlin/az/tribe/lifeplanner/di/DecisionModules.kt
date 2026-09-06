package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.repository.SqlDelightDecisionProfileRepository
import az.tribe.lifeplanner.data.repository.SqlDelightDecisionRepository
import az.tribe.lifeplanner.data.repository.SqlDelightIdentityStatementRepository
import az.tribe.lifeplanner.data.repository.SqlDelightLifeValueRepository
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.IdentityStatementRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.service.CalibrationEngine
import az.tribe.lifeplanner.domain.service.CalibrationProvider
import az.tribe.lifeplanner.domain.service.CausalInsightEngine
import az.tribe.lifeplanner.domain.service.CausalInsightProvider
import az.tribe.lifeplanner.domain.service.ChoicePointDetector
import az.tribe.lifeplanner.ui.becoming.BecomingViewModel
import az.tribe.lifeplanner.ui.causal.CausalInsightsViewModel
import az.tribe.lifeplanner.ui.decision.DecisionViewModel
import az.tribe.lifeplanner.ui.decision.MetacognitiveReviewViewModel
import az.tribe.lifeplanner.ui.wiring.WiringViewModel
import az.tribe.lifeplanner.usecases.AutoLinkGoalValuesUseCase
import az.tribe.lifeplanner.usecases.ComputeValueAlignmentUseCase
import az.tribe.lifeplanner.usecases.PromoteTopValuesToLifeValuesUseCase
import az.tribe.lifeplanner.usecases.SeedDefaultLifeValuesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

/** The Free Agents pillars: decisions, values, identity, causal insight and calibration. */
val decisionDataModule = module {
    singleOf(::SqlDelightDecisionRepository) { bind<DecisionRepository>() }
    singleOf(::SqlDelightIdentityStatementRepository) { bind<IdentityStatementRepository>() }
    singleOf(::SqlDelightDecisionProfileRepository) { bind<DecisionProfileRepository>() }
    singleOf(::SqlDelightLifeValueRepository) { bind<LifeValueRepository>() }
}

val decisionDomainModule = module {
    // The engines take tuning parameters with defaults, so they are built without injection.
    single { ChoicePointDetector() }
    single { CausalInsightEngine() }
    single { CalibrationEngine() }
    singleOf(::CausalInsightProvider)
    singleOf(::CalibrationProvider)
    factoryOf(::ComputeValueAlignmentUseCase)
    factoryOf(::PromoteTopValuesToLifeValuesUseCase)
    factoryOf(::SeedDefaultLifeValuesUseCase)
    factoryOf(::AutoLinkGoalValuesUseCase)
}

val decisionPresentationModule = module {
    viewModelOf(::DecisionViewModel)
    viewModelOf(::MetacognitiveReviewViewModel)
    viewModelOf(::CausalInsightsViewModel)
    viewModelOf(::BecomingViewModel)
    viewModelOf(::WiringViewModel)
}
