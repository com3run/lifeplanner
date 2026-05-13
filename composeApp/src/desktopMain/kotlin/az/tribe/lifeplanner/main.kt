package az.tribe.lifeplanner

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import az.tribe.lifeplanner.di.initKoin
import java.awt.Dimension

fun main() {
    initKoin()
    AppInitializer.onApplicationStart()
    application {
        val state = rememberWindowState(
            size = DpSize(1200.dp, 800.dp)
        )
        Window(
            onCloseRequest = ::exitApplication,
            title = "Life Planner",
            state = state
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(900, 620)
            }
            App()
        }
    }
}
