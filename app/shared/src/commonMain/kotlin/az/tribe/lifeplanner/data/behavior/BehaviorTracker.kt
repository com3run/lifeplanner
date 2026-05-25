package az.tribe.lifeplanner.data.behavior

import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BehaviorTracker(private val repository: BehaviorRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = Logger.withTag("BehaviorTracker")

    // Single ref swap is atomic, callers (App.kt LaunchedEffect + LifecycleEventObserver)
    // are main-thread, but this also keeps exit/enter writes pairable per nav event.
    private var session: ScreenSession? = null

    fun onScreenChanged(newRoute: String?) {
        val route = newRoute ?: return
        val now = currentEpochMs()
        val previous = session
        session = ScreenSession(route, now)
        scope.launch { recordTransition(previous, now, route) }
    }

    fun onAppBackground() {
        val previous = session ?: return
        val now = currentEpochMs()
        session = null
        scope.launch { recordExit(previous, now) }
    }

    private suspend fun recordTransition(previous: ScreenSession?, now: Long, route: String) {
        previous?.let { recordExit(it, now) }
        try {
            repository.recordScreenEnter(route)
        } catch (e: Exception) {
            log.w { "recordScreenEnter failed: ${e.message}" }
        }
    }

    private suspend fun recordExit(previous: ScreenSession, now: Long) {
        val duration = (now - previous.enterMs).coerceAtLeast(0L)
        try {
            repository.recordScreenExit(previous.route, duration)
        } catch (e: Exception) {
            log.w { "recordScreenExit failed: ${e.message}" }
        }
    }

    private fun currentEpochMs(): Long = Clock.System.now().toEpochMilliseconds()

    private data class ScreenSession(val route: String, val enterMs: Long)
}
