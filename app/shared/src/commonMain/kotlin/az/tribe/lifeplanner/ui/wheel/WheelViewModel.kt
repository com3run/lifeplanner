package az.tribe.lifeplanner.ui.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.repository.WheelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class WheelUiState(
    val report: WheelReport? = null,
    val selected: WheelArea? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class WheelViewModel(
    private val repository: WheelRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WheelUiState())
    val state: StateFlow<WheelUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWheel()
                .catch { e -> _state.value = _state.value.copy(isLoading = false, error = e.message) }
                .collect { report ->
                    _state.value = _state.value.copy(report = report, isLoading = false, error = null)
                }
        }
    }

    /** Tapping the already-selected slice closes it, so the wheel is never stuck open. */
    fun select(area: WheelArea?) {
        _state.value = _state.value.copy(
            selected = if (area == _state.value.selected) null else area
        )
    }

    fun setScore(area: WheelArea, score: Double) {
        viewModelScope.launch { repository.setScore(area, score) }
    }

    /** Hands the area back to the predictor. */
    fun clearScore(area: WheelArea) {
        viewModelScope.launch { repository.clearScore(area) }
    }
}
