package az.tribe.lifeplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import az.tribe.lifeplanner.domain.service.DayPhase
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * The current [DayPhase], kept honest while the app is open.
 *
 * Reading the hour once at composition would be right most of the time and quietly wrong for the
 * user who leaves the app open through sunset, which is exactly the moment the change is worth
 * seeing. A minute of granularity is plenty for a boundary that moves four times a day.
 */
@Composable
fun rememberDayPhase(): DayPhase {
    var phase by remember { mutableStateOf(DayPhase.of(currentHour())) }
    LaunchedEffect(Unit) {
        while (true) {
            phase = DayPhase.of(currentHour())
            delay(60_000)
        }
    }
    return phase
}

private fun currentHour(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
