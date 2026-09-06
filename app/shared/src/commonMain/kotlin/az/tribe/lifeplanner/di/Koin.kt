package az.tribe.lifeplanner.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module, assembled here once for both platforms and for the module verification test. */
val appModules: List<Module> = listOf(
    coreDataModule,
    networkModule,
    supabaseModule,
    authDataModule,
    authPresentationModule,
    goalDataModule,
    goalDomainModule,
    goalPresentationModule,
    habitDataModule,
    habitDomainModule,
    habitPresentationModule,
    journalDataModule,
    journalDomainModule,
    journalPresentationModule,
    coachDataModule,
    coachPresentationModule,
    onboardingPresentationModule,
    gamificationDataModule,
    gamificationDomainModule,
    gamificationPresentationModule,
    decisionDataModule,
    decisionDomainModule,
    decisionPresentationModule,
    possibilityDomainModule,
    possibilityPresentationModule,
    focusDataModule,
    focusPresentationModule,
    reminderDataModule,
    reminderDomainModule,
    reminderPresentationModule,
    healthDataModule,
    healthDomainModule,
    healthPresentationModule,
    calendarDataModule,
    calendarPresentationModule,
    balanceDataModule,
    balancePresentationModule,
    homeDataModule,
    homePresentationModule,
    retrospectiveDataModule,
    retrospectivePresentationModule,
    backupDataModule,
    backupPresentationModule,
    screenTimePresentationModule,
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules)
    }
