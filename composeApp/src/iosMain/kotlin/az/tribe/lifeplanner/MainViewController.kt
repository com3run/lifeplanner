package az.tribe.lifeplanner

import androidx.compose.ui.window.ComposeUIViewController
import az.tribe.lifeplanner.di.initKoin
import az.tribe.lifeplanner.ui.GoalViewModel
import org.koin.compose.koinInject

fun MainViewController() = ComposeUIViewController (
    configure = {
        initKoin()
    }
){
    val mainViewModel =  koinInject<GoalViewModel>()
    App(mainViewModel)
}