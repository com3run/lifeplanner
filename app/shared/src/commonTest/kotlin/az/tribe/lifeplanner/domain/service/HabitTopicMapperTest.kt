package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.testutil.testHabit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitTopicMapperTest {

    @Test
    fun `every habit is at least about habits`() {
        val topics = HabitTopicMapper.topicsFor(testHabit(title = "Something vague"))
        assertTrue(KnowledgeTopic.HABITS in topics)
    }

    @Test
    fun `a bedtime habit is about sleep`() {
        val topics = HabitTopicMapper.topicsFor(testHabit(title = "Be in bed by 10 PM"))
        assertTrue(KnowledgeTopic.SLEEP in topics)
    }

    @Test
    fun `a meditation habit is about the mind and attention`() {
        val topics = HabitTopicMapper.topicsFor(testHabit(title = "Morning Meditation"))
        assertTrue(KnowledgeTopic.MINDSET in topics)
        assertTrue(KnowledgeTopic.FOCUS in topics)
    }

    @Test
    fun `a reading habit is about focus`() {
        val topics = HabitTopicMapper.topicsFor(testHabit(title = "Read 20 pages"))
        assertTrue(KnowledgeTopic.FOCUS in topics)
    }

    @Test
    fun `the description is searched too, not just the title`() {
        val topics = HabitTopicMapper.topicsFor(
            testHabit(title = "Evening routine", description = "Wind down and get to bed early")
        )
        assertTrue(KnowledgeTopic.SLEEP in topics)
    }

    @Test
    fun `category is the fallback when nothing in the text matches`() {
        val topics = HabitTopicMapper.topicsFor(
            testHabit(title = "Xyzzy", category = GoalCategory.MONEY)
        )
        assertTrue(KnowledgeTopic.DECISIONS in topics)
        assertTrue(KnowledgeTopic.PLANNING in topics)
    }

    @Test
    fun `sleep comes from the words, not from a whole category`() {
        // Mapping BODY to SLEEP made every exercise habit surface sleep lessons.
        val exercise = HabitTopicMapper.topicsFor(
            testHabit(title = "Exercise", description = "30 minutes workout", category = GoalCategory.BODY)
        )
        assertFalse(KnowledgeTopic.SLEEP in exercise, "an exercise habit is not about sleep")
        assertTrue(KnowledgeTopic.MOTIVATION in exercise)
    }

    @Test
    fun `a habit tied to a goal is partly about goals`() {
        val topics = HabitTopicMapper.topicsFor(testHabit(title = "Xyzzy", linkedGoalId = "g1"))
        assertTrue(KnowledgeTopic.GOALS in topics)
    }

    @Test
    fun `the first matched topic carries the most weight`() {
        val habit = testHabit(title = "Be in bed by 10 PM")
        val affinity = HabitTopicMapper.affinityFor(habit)
        val sleep = affinity.getValue(KnowledgeTopic.SLEEP)
        val habits = affinity.getValue(KnowledgeTopic.HABITS)
        assertTrue(sleep > habits, "keyword match ($sleep) should outweigh the catch-all ($habits)")
    }

    @Test
    fun `a habit on a streak outweighs a dormant one on the same topic`() {
        val dormant = HabitTopicMapper.affinityFor(listOf(testHabit(id = "a", title = "Sleep early")))
        val active = HabitTopicMapper.affinityFor(
            listOf(testHabit(id = "a", title = "Sleep early", currentStreak = 10))
        )
        assertTrue(active.getValue(KnowledgeTopic.SLEEP) > dormant.getValue(KnowledgeTopic.SLEEP))
    }

    @Test
    fun `inactive habits do not shape recommendations`() {
        val affinity = HabitTopicMapper.affinityFor(
            listOf(testHabit(title = "Sleep early", isActive = false))
        )
        assertEquals(emptyMap(), affinity)
    }

    @Test
    fun `topics across several habits accumulate`() {
        val affinity = HabitTopicMapper.affinityFor(
            listOf(
                testHabit(id = "a", title = "Sleep early"),
                testHabit(id = "b", title = "Read 20 pages"),
            )
        )
        assertTrue(KnowledgeTopic.SLEEP in affinity)
        assertTrue(KnowledgeTopic.FOCUS in affinity)
        assertTrue(affinity.getValue(KnowledgeTopic.HABITS) > 0.0)
    }

    @Test
    fun `every mapped topic exists in the library so recommendations can never be empty`() {
        // A topic no lesson is tagged with would be a silent dead end in the recommender.
        val libraryTopics = KnowledgeLibrary.all.flatMap { it.topics }.toSet()
        for (category in GoalCategory.entries) {
            val topics = HabitTopicMapper.topicsFor(testHabit(title = "Xyzzy", category = category))
            assertTrue(
                topics.any { it in libraryTopics },
                "no lesson covers any topic mapped for $category: $topics"
            )
        }
    }
}
