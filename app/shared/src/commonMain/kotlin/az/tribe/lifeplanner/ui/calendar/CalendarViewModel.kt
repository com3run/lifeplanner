package az.tribe.lifeplanner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.calendar.CalendarPreferences
import az.tribe.lifeplanner.data.calendar.CalendarReader
import az.tribe.lifeplanner.domain.model.CalendarEvent
import az.tribe.lifeplanner.domain.model.DeviceCalendar
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

/** A device calendar plus whether the user currently imports it. */
data class CalendarSelection(
    val calendar: DeviceCalendar,
    val enabled: Boolean,
)

class CalendarViewModel(
    private val calendarReader: CalendarReader,
    private val calendarPreferences: CalendarPreferences,
) : ViewModel() {

    // Raw reads are kept unfiltered and the user's calendar selection is applied downstream, so a
    // toggle on the settings screen updates every screen without re-querying the device.
    private val _rawEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = filteredBySelection(_rawEvents)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    /** Events for the currently selected day (day-lens view). Independent of the upcoming list. */
    private val _rawDayEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val dayEvents: StateFlow<List<CalendarEvent>> = filteredBySelection(_rawDayEvents)

    /** Every calendar the device exposes, paired with the user's import toggle. */
    private val _deviceCalendars = MutableStateFlow<List<DeviceCalendar>>(emptyList())
    val calendars: StateFlow<List<CalendarSelection>> =
        combine(_deviceCalendars, calendarPreferences.disabledIds) { calendars, disabled ->
            calendars.map { CalendarSelection(it, it.id !in disabled) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _calendarsLoading = MutableStateFlow(false)
    val calendarsLoading: StateFlow<Boolean> = _calendarsLoading.asStateFlow()

    private var loadedDay: LocalDate? = null

    private fun filteredBySelection(source: StateFlow<List<CalendarEvent>>): StateFlow<List<CalendarEvent>> =
        combine(source, calendarPreferences.disabledIds) { events, disabled ->
            if (disabled.isEmpty()) events
            // Events with no calendar id (older platform paths) are kept rather than dropped.
            else events.filter { it.calendarId == null || it.calendarId !in disabled }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun load(days: Int = 7) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _rawEvents.value = calendarReader.readUpcomingEvents(days)
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
                _rawDayEvents.value = calendarReader.readEvents(start, end)
            } catch (e: Exception) {
                Logger.w("CalendarViewModel") { "Failed to load day events: ${e.message}" }
                _rawDayEvents.value = emptyList()
            }
        }
    }

    fun loadCalendars() {
        viewModelScope.launch {
            _calendarsLoading.value = true
            try {
                _deviceCalendars.value = calendarReader.listCalendars()
            } catch (e: Exception) {
                Logger.w("CalendarViewModel") { "Failed to list calendars: ${e.message}" }
                _deviceCalendars.value = emptyList()
            } finally {
                _calendarsLoading.value = false
            }
        }
    }

    fun setCalendarEnabled(calendarId: String, enabled: Boolean) =
        calendarPreferences.setEnabled(calendarId, enabled)

    fun enableAllCalendars() = calendarPreferences.enableAll()
}
