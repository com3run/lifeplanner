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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class CalendarViewModel(
    private val calendarReader: CalendarReader,
) : ViewModel() {

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    /** Events for the currently selected day (day-lens view). Independent of the upcoming list. */
    private val _dayEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val dayEvents: StateFlow<List<CalendarEvent>> = _dayEvents.asStateFlow()

    private var loadedDay: LocalDate? = null

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

    /** Load the device-calendar events that fall on [date] (local time), for the day lens. */
    fun loadDay(date: LocalDate) {
        if (loadedDay == date) return
        loadedDay = date
        viewModelScope.launch {
            val tz = TimeZone.currentSystemDefault()
            val start = date.atStartOfDayIn(tz).toEpochMilliseconds()
            val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz).toEpochMilliseconds()
            try {
                _dayEvents.value = calendarReader.readEvents(start, end)
            } catch (e: Exception) {
                Logger.w("CalendarViewModel") { "Failed to load day events: ${e.message}" }
                _dayEvents.value = emptyList()
            }
        }
    }
}
