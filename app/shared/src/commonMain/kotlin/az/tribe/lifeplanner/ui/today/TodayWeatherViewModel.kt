package az.tribe.lifeplanner.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.TodayWeather
import az.tribe.lifeplanner.domain.repository.WeatherRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backs the weather strip on the Today surface. The screen owns the (Compose) location permission and
 * feeds its state in via [onPermissionState]; this VM only fetches once granted and exposes UI state.
 */
class TodayWeatherViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    sealed interface State {
        data object Idle : State            // haven't determined permission yet
        data object NeedsPermission : State // denied / not yet granted
        data object Loading : State
        data class Loaded(val weather: TodayWeather) : State
        data object Unavailable : State     // granted but no data (offline / no fix)
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    /** Called whenever the location permission state is known/changes. */
    fun onPermissionState(granted: Boolean) {
        if (!granted) {
            _state.value = State.NeedsPermission
            return
        }
        // Fetch once; don't thrash on recompositions after we already have data.
        if (_state.value is State.Loaded || _state.value is State.Loading) return
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = State.Loading
            val weather = runCatching { weatherRepository.today() }
                .onFailure { Logger.w("TodayWeatherViewModel") { "weather fetch failed: ${it.message}" } }
                .getOrNull()
            _state.value = if (weather != null) State.Loaded(weather) else State.Unavailable
        }
    }
}
