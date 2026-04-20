package az.tribe.lifeplanner.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkNavigator {
    private val _navEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navEvents = _navEvents.asSharedFlow()

    fun navigate(route: String) {
        _navEvents.tryEmit(route)
    }
}
