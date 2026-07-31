package az.tribe.lifeplanner.usecases.habit

import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.service.HabitTopicMapper
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.KnowledgeRecommender
import az.tribe.lifeplanner.domain.service.KnowledgeSignals
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Picks the Learn lessons that speak to one specific habit, so the knowledge shows up next to the
 * practice: sleep science on a bedtime habit, attention research on a focus habit.
 *
 * Ranks the whole unlocked library against what the habit is about ([HabitTopicMapper]) rather than
 * against the user's overall activity, which is what the Learn hub does.
 */
class RecommendLessonsForHabitUseCase(
    private val knowledgeRepository: KnowledgeRepository,
    private val gamificationRepository: GamificationRepository,
) {
    /** The [count] best lessons for [habit], best first. Read ones sink but are not removed. */
    suspend operator fun invoke(habit: Habit, count: Int = 3): List<KnowledgeBit> =
        KnowledgeRecommender.rank(signalsFor(habit)).take(count)

    /** The single best *unread* lesson for [habit], for a one-shot moment like a check-in. */
    suspend fun bestUnread(habit: Habit): KnowledgeBit? =
        KnowledgeRecommender.recommend(signalsFor(habit), count = 1).firstOrNull()

    private suspend fun signalsFor(habit: Habit) = KnowledgeSignals(
        level = runCatching { gamificationRepository.getUserProgress().first().currentLevel }
            .getOrDefault(1),
        readIds = runCatching { knowledgeRepository.readIds().first() }.getOrDefault(emptySet()),
        topicAffinity = HabitTopicMapper.affinityFor(habit),
        daySeed = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfYear,
    )
}
