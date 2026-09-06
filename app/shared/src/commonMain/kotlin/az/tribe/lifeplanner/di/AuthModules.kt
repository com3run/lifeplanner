package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.data.auth.AuthService
import az.tribe.lifeplanner.data.auth.SupabaseAuthService
import az.tribe.lifeplanner.data.auth.supabaseAuthTokenProvider
import az.tribe.lifeplanner.data.network.AuthTokenProvider
import az.tribe.lifeplanner.data.repository.SqlDelightUserRepository
import az.tribe.lifeplanner.data.repository.SqlDelightUserSituationRepository
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    singleOf(::SupabaseAuthService) { bind<AuthService>() }
    // A factory function rather than a constructor: the provider wraps the live Supabase session.
    single<AuthTokenProvider> { supabaseAuthTokenProvider(get()) }
    singleOf(::SqlDelightUserRepository) { bind<UserRepository>() }
    singleOf(::SqlDelightUserSituationRepository) { bind<UserSituationRepository>() }
}

val authPresentationModule = module {
    viewModelOf(::AuthViewModel)
}
