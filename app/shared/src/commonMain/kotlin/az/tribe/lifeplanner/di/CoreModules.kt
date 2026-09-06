package az.tribe.lifeplanner.di

import az.tribe.lifeplanner.core.DefaultPremiumGate
import az.tribe.lifeplanner.core.PremiumGate
import az.tribe.lifeplanner.data.behavior.BehaviorTracker
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.data.network.EdgeFunctionAiProxyService
import az.tribe.lifeplanner.data.repository.SqlDelightBehaviorRepository
import az.tribe.lifeplanner.data.repository.SupabaseAiUsageRepository
import az.tribe.lifeplanner.domain.repository.AiUsageRepository
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.location.LocationProvider
import az.tribe.lifeplanner.notification.NotificationSchedulerInterface
import az.tribe.lifeplanner.notification.getNotificationScheduler
import az.tribe.lifeplanner.ui.theme.ThemeController
import az.tribe.lifeplanner.util.NetworkConnectivityObserver
import az.tribe.lifeplanner.widget.WidgetDataSyncService
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

/**
 * Infrastructure every feature shares: the database, settings, platform services, the AI proxy,
 * premium gating and behavior tracking. Network clients live in [networkModule] and
 * [supabaseModule]; feature modules sit next to this file, one per feature layer.
 */
val coreDataModule = module {
    singleOf(::DatabaseDriverFactory)
    singleOf(::SharedDatabase)
    single { Settings() }
    singleOf(::ThemeController)
    single<FileSharer> { createFileSharer() }
    singleOf(::WidgetDataSyncService)
    singleOf(::NetworkConnectivityObserver)
    single<NotificationSchedulerInterface> { getNotificationScheduler() }
    singleOf(::LocationProvider)
    singleOf(::EdgeFunctionAiProxyService) { bind<AiProxyService>() }
    singleOf(::SupabaseAiUsageRepository) { bind<AiUsageRepository>() }
    singleOf(::DefaultPremiumGate) { bind<PremiumGate>() }
    singleOf(::SqlDelightBehaviorRepository) { bind<BehaviorRepository>() }
    singleOf(::BehaviorTracker)
}
