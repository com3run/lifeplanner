package az.tribe.lifeplanner

import androidx.compose.ui.window.ComposeUIViewController
import az.tribe.lifeplanner.di.initKoin
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import org.koin.compose.koinInject

fun MainViewController() = ComposeUIViewController (
    configure = {
        initKoin()
        // RevenueCat — in-app subscriptions / paywalls / customer center
        if (BuildKonfig.REVENUECAT_IOS_API_KEY.isNotBlank() && !Purchases.isConfigured) {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                PurchasesConfiguration(apiKey = BuildKonfig.REVENUECAT_IOS_API_KEY) { appUserId = null }
            )
        }
    }
){
    val mainViewModel =  koinInject<GoalViewModel>()
    App(mainViewModel)
}