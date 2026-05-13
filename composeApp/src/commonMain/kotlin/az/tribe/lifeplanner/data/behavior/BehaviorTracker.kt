package az.tribe.lifeplanner.data.behavior

import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BehaviorTracker(private val repository: BehaviorRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentScreen: String? = null
    private var enterEpochMs: Long = 0L

    fun onScreenChanged(newRoute: String?) {
        val route = newRoute ?: return
        val now = currentEpochMs()

        // Record exit for previous screen
        currentScreen?.let { prev ->
            val duration = (now - enterEpochMs).coerceAtLeast(0L)
            scope.launch {
                try { repository.recordScreenExit(prev, duration) }
                catch (e: Exception) { Logger.w("BehaviorTracker") { "recordScreenExit failed: ${e.message}" } }
            }
        }

        // Record enter for new screen
        currentScreen = route
        enterEpochMs = now
        scope.launch {
            try { repository.recordScreenEnter(route) }
            catch (e: Exception) { Logger.w("BehaviorTracker") { "recordScreenEnter failed: ${e.message}" } }
        }
    }

    fun onAppBackground() {
        currentScreen?.let { prev ->
            val duration = (currentEpochMs() - enterEpochMs).coerceAtLeast(0L)
            scope.launch {
                try { repository.recordScreenExit(prev, duration) }
                catch (e: Exception) { /* silent */ }
            }
            currentScreen = null
        }
    }

    private fun currentEpochMs(): Long =
        kotlin.time.Clock.System.now().toEpochMilliseconds()
}
