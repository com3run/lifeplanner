package az.tribe.lifeplanner.data.calendar

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Which device calendars the user wants imported. Opt-out rather than opt-in: a calendar the user
 * has never touched counts as enabled, so newly added accounts show up without a second setup step.
 * Only the exclusions are persisted (newline-separated ids under [KEY_DISABLED]).
 *
 * Singleton, and [disabledIds] is a flow, so a toggle on the Calendar settings screen is picked up
 * by every other screen holding its own CalendarViewModel (e.g. the Integrations row on Profile).
 */
class CalendarPreferences(private val settings: Settings) {

    private val _disabledIds = MutableStateFlow(readDisabled())
    val disabledIds: StateFlow<Set<String>> = _disabledIds.asStateFlow()

    private fun readDisabled(): Set<String> =
        settings.getStringOrNull(KEY_DISABLED)
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

    fun isEnabled(calendarId: String): Boolean = calendarId !in _disabledIds.value

    fun setEnabled(calendarId: String, enabled: Boolean) {
        val next = _disabledIds.value.toMutableSet().apply {
            if (enabled) remove(calendarId) else add(calendarId)
        }
        if (next.isEmpty()) settings.remove(KEY_DISABLED)
        else settings.putString(KEY_DISABLED, next.joinToString(SEPARATOR))
        _disabledIds.value = next
    }

    /** Turn every calendar back on. */
    fun enableAll() {
        settings.remove(KEY_DISABLED)
        _disabledIds.value = emptySet()
    }

    companion object {
        const val KEY_DISABLED = "calendar_disabled_ids"
        private const val SEPARATOR = "\n"
    }
}
