package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.network.BuiltinCoachFetcher
import az.tribe.lifeplanner.data.network.PersonaApiFetcher
import az.tribe.lifeplanner.data.network.SystemPromptFetcher
import az.tribe.lifeplanner.data.repository.CoachOrchestrator
import az.tribe.lifeplanner.data.repository.SqlDelightChatRepository
import az.tribe.lifeplanner.data.repository.SqlDelightCoachRepository
import az.tribe.lifeplanner.data.repository.SupabaseCoachPostRepository
import az.tribe.lifeplanner.data.review.ReviewMessageBuilder
import az.tribe.lifeplanner.domain.repository.ChatRepository
import az.tribe.lifeplanner.domain.repository.CoachPostRepository
import az.tribe.lifeplanner.domain.repository.CoachRepository
import az.tribe.lifeplanner.ui.chat.ChatViewModel
import az.tribe.lifeplanner.ui.coach.CoachViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val coachDataModule = module {
    singleOf(::SqlDelightCoachRepository) { bind<CoachRepository>() }
    singleOf(::SupabaseCoachPostRepository) { bind<CoachPostRepository>() }
    singleOf(::CoachOrchestrator)
    singleOf(::SqlDelightChatRepository) { bind<ChatRepository>() }
    singleOf(::ReviewMessageBuilder)
    singleOf(::BuiltinCoachFetcher)
    singleOf(::PersonaApiFetcher)
    singleOf(::SystemPromptFetcher)
}

val coachPresentationModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::CoachViewModel)
}
