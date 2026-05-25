package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.firstOrNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * One-time, idempotent migration (Pillar 1): promote the user's onboarding values
 * (`UserSituation.purpose.topValues`) into first-class [LifeValue] rows. The data
 * already exists, so no new onboarding screen is needed.
 *
 * Idempotency: guarded by a settings flag. The flag is only set once a non-empty
 * `topValues` has been processed, so a user who onboards *after* updating still
 * gets seeded on a later launch. Existing values (by title) are never duplicated.
 */
class PromoteTopValuesToLifeValuesUseCase(
    private val lifeValueRepository: LifeValueRepository,
    private val userSituationRepository: UserSituationRepository,
    private val settings: Settings
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke() {
        if (settings.getBoolean(FLAG, false)) return
        try {
            val situation = userSituationRepository.observe().firstOrNull() ?: return
            val topValues = situation.purpose.topValues
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            // Nothing to migrate yet (e.g. onboarding not done), retry next launch.
            if (topValues.isEmpty()) return

            val existingTitles = lifeValueRepository.getAllLifeValues()
                .map { it.title.lowercase().trim() }
                .toSet()
            val newValues = topValues
                .filterNot { it.lowercase() in existingTitles }
                .mapIndexed { index, title ->
                    LifeValue(
                        id = Uuid.random().toString(),
                        title = title,
                        description = "",
                        isActive = true,
                        order = existingTitles.size + index
                    )
                }
            if (newValues.isNotEmpty()) {
                lifeValueRepository.insertLifeValues(newValues)
                Logger.d("LifeValueMigration") {
                    "Promoted ${newValues.size} onboarding value(s) to LifeValue rows"
                }
            }
            settings.putBoolean(FLAG, true)
        } catch (e: Exception) {
            // Leave the flag unset so it retries on the next launch.
            Logger.w("LifeValueMigration") { "topValues → LifeValue migration failed: ${e.message}" }
        }
    }

    companion object {
        private const val FLAG = "life_values_seeded_from_top_values_v1"
    }
}
