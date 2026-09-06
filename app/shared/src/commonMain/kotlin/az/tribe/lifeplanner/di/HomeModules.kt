package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.network.KnowledgeFetcher
import az.tribe.lifeplanner.data.network.OpenMeteoWeatherRepository
import az.tribe.lifeplanner.data.repository.SqlDelightKnowledgeRepository
import az.tribe.lifeplanner.data.repository.SupabaseStoryRepository
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.repository.StoryRepository
import az.tribe.lifeplanner.domain.repository.WeatherRepository
import az.tribe.lifeplanner.ui.foryou.ForYouViewModel
import az.tribe.lifeplanner.ui.foryou.HomeFeedBuilder
import az.tribe.lifeplanner.ui.foryou.KnowledgeDetailViewModel
import az.tribe.lifeplanner.ui.foryou.LearnHubViewModel
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.intro.IntroSeenStore
import az.tribe.lifeplanner.ui.intro.SettingsIntroSeenStore
import az.tribe.lifeplanner.ui.profile.YouViewModel
import az.tribe.lifeplanner.ui.search.SearchViewModel
import az.tribe.lifeplanner.ui.today.TodayViewModel
import az.tribe.lifeplanner.ui.today.TodayWeatherViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

/** The home surfaces: For You, Today, Learn, You and search, plus the content they draw on. */
val homeDataModule = module {
    singleOf(::SqlDelightKnowledgeRepository) { bind<KnowledgeRepository>() }
    singleOf(::KnowledgeFetcher)
    singleOf(::SupabaseStoryRepository) { bind<StoryRepository>() }
    singleOf(::OpenMeteoWeatherRepository) { bind<WeatherRepository>() }
    single<IntroSeenStore> { SettingsIntroSeenStore() }
}

val homePresentationModule = module {
    // One constructor parameter has a default the container cannot supply, hence the lambda.
    single { HomeFeedBuilder(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModelOf(::ForYouViewModel)
    viewModelOf(::TodayViewModel)
    viewModelOf(::TodayWeatherViewModel)
    viewModelOf(::LearnHubViewModel)
    viewModelOf(::KnowledgeDetailViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::YouViewModel)
    viewModelOf(::SearchViewModel)
}
