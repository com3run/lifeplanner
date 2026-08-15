package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.Habit

/**
 * Works out what a habit is *about*, so the Learn library can teach the user about the thing they
 * are actually doing. Someone running a sleep habit should meet the sleep lessons; someone building
 * a meditation practice should meet the ones on attention and the mind.
 *
 * Pure and deterministic: title and description keywords first (they are specific), the habit's
 * category as a fallback (it is always present). Every habit is at least about [KnowledgeTopic.HABITS],
 * because keeping any habit is a skill in itself.
 */
object HabitTopicMapper {

    private val keywordTopics: List<Pair<List<String>, List<KnowledgeTopic>>> = listOf(
        listOf("sleep", "bed", "bedtime", "wake", "rest", "nap") to
            listOf(KnowledgeTopic.SLEEP),
        listOf("meditat", "breath", "mindful", "calm", "gratitude", "pray", "journal", "reflect") to
            listOf(KnowledgeTopic.MINDSET, KnowledgeTopic.FOCUS),
        listOf("read", "study", "learn", "practice", "write", "code", "deep work", "focus") to
            listOf(KnowledgeTopic.FOCUS),
        listOf("plan", "review", "prioriti", "schedule", "weekly", "inbox") to
            listOf(KnowledgeTopic.PLANNING),
        listOf("run", "walk", "gym", "workout", "exercise", "train", "yoga", "stretch", "steps") to
            listOf(KnowledgeTopic.MOTIVATION),
        listOf("budget", "save", "spend", "invest", "expense") to
            listOf(KnowledgeTopic.DECISIONS, KnowledgeTopic.PLANNING),
        listOf("no ", "quit", "stop", "avoid", "less ") to
            listOf(KnowledgeTopic.DECISIONS, KnowledgeTopic.MINDSET),
    )

    /**
     * The fallback when the text says nothing specific. Deliberately narrow: [KnowledgeTopic.SLEEP]
     * is left to the keywords, because inferring it from a whole category made every exercise habit
     * surface sleep lessons.
     */
    private val categoryTopics: Map<GoalCategory, List<KnowledgeTopic>> = mapOf(
        GoalCategory.CAREER to listOf(KnowledgeTopic.FOCUS, KnowledgeTopic.GOALS),
        GoalCategory.MONEY to listOf(KnowledgeTopic.DECISIONS, KnowledgeTopic.PLANNING),
        GoalCategory.BODY to listOf(KnowledgeTopic.MOTIVATION),
        GoalCategory.PEOPLE to listOf(KnowledgeTopic.MOTIVATION),
        GoalCategory.WELLBEING to listOf(KnowledgeTopic.MINDSET),
        GoalCategory.PURPOSE to listOf(KnowledgeTopic.MINDSET, KnowledgeTopic.GOALS),
        GoalCategory.FAMILY to listOf(KnowledgeTopic.PLANNING),
    )

    /** What this habit is about, most specific first, always ending with [KnowledgeTopic.HABITS]. */
    fun topicsFor(habit: Habit): List<KnowledgeTopic> {
        val text = (habit.title + " " + habit.description).lowercase()
        val fromKeywords = keywordTopics
            .filter { (words, _) -> words.any { it in text } }
            .flatMap { it.second }
        val fromCategory = categoryTopics[habit.category].orEmpty()
        // A habit tied to a goal is also, in part, about goals.
        val fromGoal = if (habit.linkedGoalId != null) listOf(KnowledgeTopic.GOALS) else emptyList()
        return (fromKeywords + fromCategory + fromGoal + KnowledgeTopic.HABITS).distinct()
    }

    /**
     * Topic weights for one habit, highest for the most specific match. [activityBoost] scales the
     * whole habit's contribution so a habit the user is actually keeping outranks a dormant one.
     */
    fun affinityFor(habit: Habit, activityBoost: Double = 1.0): Map<KnowledgeTopic, Double> {
        val topics = topicsFor(habit)
        return topics.mapIndexed { index, topic ->
            // First match is the strongest signal; later ones taper but never reach zero.
            topic to (activityBoost * (1.0 / (1.0 + index)))
        }.toMap()
    }

    /**
     * Combined topic weights across [habits], with each habit weighted by how alive it is. A streak
     * counts for more than a habit that exists but is never done, so the Learn feed follows the
     * user's real practice rather than their intentions.
     */
    fun affinityFor(habits: List<Habit>): Map<KnowledgeTopic, Double> {
        val totals = mutableMapOf<KnowledgeTopic, Double>()
        for (habit in habits) {
            if (!habit.isActive) continue
            val boost = 1.0 + habit.currentStreak.coerceIn(0, MAX_STREAK_BOOST) * STREAK_WEIGHT
            for ((topic, weight) in affinityFor(habit, boost)) {
                totals[topic] = (totals[topic] ?: 0.0) + weight
            }
        }
        return totals
    }

    private const val MAX_STREAK_BOOST = 14
    private const val STREAK_WEIGHT = 0.25
}
