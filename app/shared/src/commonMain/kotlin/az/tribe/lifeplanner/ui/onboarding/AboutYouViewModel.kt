package az.tribe.lifeplanner.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.LifeStage
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AboutYouState(
    val name: String = "",
    val age: Int? = null,
    val stress: Int? = null,
    val sleep: Int? = null,
)

/**
 * The state behind [AboutYouScreen].
 *
 * Deliberately thin. The old flow carried this in a ViewModel with dozens of fields, a phase
 * machine and its own analytics, because it was a thirteen-screen conversation that had to be
 * resumable halfway through. A form you can see the end of does not need any of that: you fill in
 * what you want and press Save.
 */
class AboutYouViewModel(
    private val userSituationRepository: UserSituationRepository,
    private val userRepository: UserRepository,
    private val settings: Settings,
) : ViewModel() {

    private val _state = MutableStateFlow(AboutYouState())
    val state: StateFlow<AboutYouState> = _state.asStateFlow()

    fun setName(value: String) { _state.value = _state.value.copy(name = value) }
    fun setAge(value: Int?) { _state.value = _state.value.copy(age = value) }
    fun setStress(value: Int) { _state.value = _state.value.copy(stress = value) }
    fun setSleep(value: Int) { _state.value = _state.value.copy(sleep = value) }

    /**
     * Saves whatever was filled in and marks setup handled, so the Today card stops offering it.
     *
     * Best-effort on the write: a user who typed their name and pressed Save should not be sent
     * back to a card telling them to do it again because the network was down.
     */
    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            runCatching {
                val userId = userRepository.getCurrentUser()?.id
                if (userId != null) {
                    userSituationRepository.upsert(
                        userId,
                        UserSituation(
                            meta = MetaSlice(
                                name = s.name.takeIf { it.isNotBlank() },
                                age = s.age,
                                lifeStage = s.age?.let(::lifeStageFor),
                                stressLevel = s.stress ?: 5,
                                sleepQuality = s.sleep ?: 7,
                                // Answered directly by the user rather than inferred, but only as
                                // much of it as they chose to fill in.
                                confidence = if (s.name.isBlank() && s.age == null &&
                                    s.stress == null && s.sleep == null
                                ) 0f else 0.7f,
                            ),
                            lastUpdatedBy = "about_you",
                        ),
                    )
                }
            }
            settings.putBoolean(KEY_HANDLED, true)
            onDone()
        }
    }

    /** "Not now" is an answer too, and it means stop asking. */
    fun decline() {
        settings.putBoolean(KEY_HANDLED, true)
    }

    private fun lifeStageFor(age: Int): LifeStage? = when {
        age < 18 -> null
        age <= 35 -> LifeStage.EARLY_CAREER
        age <= 55 -> LifeStage.MID_CAREER
        else -> LifeStage.SENIOR
    }

    companion object {
        const val KEY_HANDLED = "about_you_handled"

        fun isHandled(settings: Settings) = settings.getBoolean(KEY_HANDLED, false)
    }
}
