package az.tribe.lifeplanner.ui.wiring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.TuningDial
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 7 (Innate), backs the "Your Wiring" screen: exposes the inferred [DecisionProfile]
 * and lets the user nudge a dial when the inference feels wrong. It's a description the user
 * owns, never a score, so a manual nudge is treated as strong, confident evidence.
 */
class WiringViewModel(
    private val repository: DecisionProfileRepository,
) : ViewModel() {

    val profile: StateFlow<DecisionProfile?> =
        repository.observeProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalUuidApi::class)
    fun nudge(dial: TuningDial, value: Float) {
        viewModelScope.launch {
            val current = repository.getProfile() ?: DecisionProfile.neutral(Uuid.random().toString())
            val prior = current.dial(dial)
            // The user telling us directly is strong evidence: mark it reliable so it sticks.
            val nudged = DialSetting(
                value = value.coerceIn(0f, 1f),
                confidence = 1f,
                sampleSize = maxOf(prior.sampleSize, DialSetting.MIN_SAMPLES),
            )
            repository.upsertProfile(current.withDial(dial, nudged))
        }
    }
}
