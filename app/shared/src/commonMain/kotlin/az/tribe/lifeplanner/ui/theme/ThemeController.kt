package az.tribe.lifeplanner.ui.theme

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's appearance preference ([ThemeMode]) and exposes it as observable state.
 *
 * Defaults to [ThemeMode.SYSTEM] so the app follows the OS, replacing the old hardcoded dark
 * default (D3 audit G2). The visible Light / Dark / System control lands with the Settings screen
 * (D7, under You → ⚙︎); this is its backing — call [setMode] from that toggle.
 */
class ThemeController(private val settings: Settings) {

    private val _mode = MutableStateFlow(read())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        settings.putString(KEY, mode.name)
        _mode.value = mode
    }

    private fun read(): ThemeMode =
        settings.getStringOrNull(KEY)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    companion object {
        private const val KEY = "theme_mode"
    }
}
