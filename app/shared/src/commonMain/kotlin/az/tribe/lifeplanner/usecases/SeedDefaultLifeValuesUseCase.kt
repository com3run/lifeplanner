package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.service.GoalValueInferrer
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 1: give every life area a "why" to link to. Life values otherwise only exist if the user
 * typed some during onboarding (see [PromoteTopValuesToLifeValuesUseCase]); a user with none, or with
 * a sparse set, left whole categories with nothing for [GoalValueInferrer] to match, so those goals
 * fell back to bare category framing.
 *
 * This seeds one sensible default value for each [GoalCategory] that isn't already represented,
 * worded so the inferrer confidently maps category-aligned goals to it. One-time and idempotent:
 * - runs once (settings flag), so it never re-adds after the user deletes something;
 * - only fills the *missing* categories, so it never duplicates values the user already has;
 * - must run *after* onboarding promotion so those values are counted as coverage.
 */
class SeedDefaultLifeValuesUseCase(
    private val lifeValueRepository: LifeValueRepository,
    private val settings: Settings,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke() {
        if (settings.getBoolean(FLAG, false)) return
        try {
            val existing = lifeValueRepository.getAllLifeValues()
            val covered = GoalValueInferrer.coveredCategories(existing)
            val missing = DEFAULTS.filterKeys { it !in covered }
            if (missing.isNotEmpty()) {
                var order = existing.size
                val seeded = missing.entries
                    .sortedBy { it.key.order }
                    .map { (_, def) ->
                        LifeValue(
                            id = Uuid.random().toString(),
                            title = def.first,
                            description = def.second,
                            isActive = true,
                            order = order++,
                        )
                    }
                lifeValueRepository.insertLifeValues(seeded)
                Logger.i(TAG) { "Seeded ${seeded.size} default life value(s) for uncovered categories" }
            }
            settings.putBoolean(FLAG, true)
        } catch (e: Exception) {
            // Leave the flag unset so it retries on the next launch.
            Logger.w(TAG) { "Default life-value seeding failed: ${e.message}" }
        }
    }

    companion object {
        // v2: coverage-based fill (v1 only seeded when the user had zero values).
        private const val FLAG = "default_life_values_seeded_v2"
        private const val TAG = "SeedDefaultLifeValues"

        /**
         * One default value per category (Title to Description). Descriptions are natural sentences
         * that also carry the category's own vocabulary, so [GoalValueInferrer] links matching goals.
         */
        private val DEFAULTS: Map<GoalCategory, Pair<String, String>> = mapOf(
            GoalCategory.CAREER to ("Craft & Career" to "Doing meaningful work and growing my skill and impact."),
            GoalCategory.MONEY to ("Financial Freedom" to "Building security and freedom with money I can rely on."),
            GoalCategory.BODY to ("Health & Vitality" to "Caring for my body with fitness, strength, and energy."),
            GoalCategory.PEOPLE to ("Connection" to "Nurturing friendship, community, and a sense of belonging."),
            GoalCategory.WELLBEING to ("Peace of Mind" to "Staying calm and balanced, protecting my mental wellbeing."),
            GoalCategory.PURPOSE to ("Purpose & Growth" to "Living with meaning, mindfulness, and continual growth."),
            GoalCategory.FAMILY to ("Family & Home" to "Showing up for my family and building a home and legacy."),
        )
    }
}
