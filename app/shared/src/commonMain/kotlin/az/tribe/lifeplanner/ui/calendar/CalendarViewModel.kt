package az.tribe.lifeplanner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.calendar.CalendarReader
import az.tribe.lifeplanner.domain.model.CalendarEvent
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val calendarReader: CalendarReader,
) : ViewModel() {

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    fun load(days: Int = 7) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _events.value = calendarReader.readUpcomingEvents(days)
            } catch (e: Exception) {
                Logger.w("CalendarViewModel") { "Failed to load calendar events: ${e.message}" }
            } finally {
                _isLoading.value = false
                _loaded.value = true
            }
        }
    }
}
