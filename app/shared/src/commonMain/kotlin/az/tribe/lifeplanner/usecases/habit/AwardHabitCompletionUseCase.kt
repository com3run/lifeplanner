package az.tribe.lifeplanner.usecases.habit

import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.usecases.ability.AwardAbilityXpUseCase
import com.russhwolf.settings.Settings
import kotlinx.datetime.LocalDate

/**
 * The single reward path for "a habit just got completed", wherever the completion came from:
 * the user tapping a card, or an in-app session crediting it. Awards the check-in XP, feeds any
 * linked abilities, and adds the perfect-day bonus the first time every habit is done that day.
 *
 * Returns the total XP awarded, so the caller can tell the user what they just earned.
 */
class AwardHabitCompletionUseCase(
    private val habitRepository: HabitRepository,
    private val gamificationRepository: GamificationRepository,
    private val awardAbilityXpUseCase: AwardAbilityXpUseCase,
    private val settings: Settings,
) {
    suspend operator fun invoke(habitId: String, date: LocalDate): Int {
        var awarded = XpRewards.HABIT_CHECK_IN
        gamificationRepository.awardXp(XpRewards.HABIT_CHECK_IN.toLong())
        if (FeatureFlags.ABILITIES_ENABLED) {
            awardAbilityXpUseCase(habitId)
        }
        if (awardPerfectDayIfEarned(date)) {
            awarded += XpRewards.PERFECT_DAY_BONUS
        }
        return awarded
    }

    /** Once per day, when nothing is left unchecked. Returns whether the bonus was granted. */
    private suspend fun awardPerfectDayIfEarned(date: LocalDate): Boolean {
        val dateStr = date.toString()
        if (settings.getStringOrNull(PREF_PERFECT_DAY_DATE) == dateStr) return false

        val allHabitIds = runCatching { habitRepository.getAllHabits() }
            .getOrDefault(emptyList())
            .map { it.id }
            .toSet()
        if (allHabitIds.isEmpty()) return false

        val completedIds = runCatching { habitRepository.getAllCheckInsInRange(date, date) }
            .getOrDefault(emptyList())
            .filter { it.completed }
            .map { it.habitId }
            .toSet()
        if (!completedIds.containsAll(allHabitIds)) return false

        gamificationRepository.awardXp(XpRewards.PERFECT_DAY_BONUS.toLong())
        settings.putString(PREF_PERFECT_DAY_DATE, dateStr)
        return true
    }

    companion object {
        const val PREF_PERFECT_DAY_DATE = "gamification_perfect_day_date"
    }
}
