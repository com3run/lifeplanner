package az.tribe.lifeplanner.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class WeeklyPlannerViewModel(private val settings: Settings) : ViewModel() {

    fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _weekStart = MutableStateFlow(mondayOf(today()))
    val weekStart: StateFlow<LocalDate> = _weekStart.asStateFlow()

    private val _selectedDay = MutableStateFlow(today())
    val selectedDay: StateFlow<LocalDate> = _selectedDay.asStateFlow()

    private val _weekIntention = MutableStateFlow("")
    val weekIntention: StateFlow<String> = _weekIntention.asStateFlow()

    init {
        _weekIntention.value = settings.getString(intentionKey(), "")
    }

    fun selectDay(date: LocalDate) {
        _selectedDay.value = date
    }

    fun previousWeek() {
        _weekStart.value = _weekStart.value.minus(7, DateTimeUnit.DAY)
        _selectedDay.value = _weekStart.value
        _weekIntention.value = settings.getString(intentionKey(), "")
    }

    fun nextWeek() {
        _weekStart.value = _weekStart.value.plus(7, DateTimeUnit.DAY)
        _selectedDay.value = _weekStart.value
        _weekIntention.value = settings.getString(intentionKey(), "")
    }

    fun updateIntention(text: String) {
        _weekIntention.value = text
        viewModelScope.launch { settings.putString(intentionKey(), text) }
    }

    fun weekDays(): List<LocalDate> =
        (0..6).map { _weekStart.value.plus(it, DateTimeUnit.DAY) }

    fun isCurrentWeek(): Boolean = mondayOf(today()) == _weekStart.value

    fun mondayOf(date: LocalDate): LocalDate =
        date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

    private fun intentionKey(): String = with(_weekStart.value) {
        "week_intention_${year}_${monthNumber}_${dayOfMonth}"
    }
}
