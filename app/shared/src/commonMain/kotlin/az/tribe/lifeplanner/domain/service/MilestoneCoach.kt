package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.data.repository.GoalTemplateProvider
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.GoalTemplate

/**
 * The coach's first draft of a goal's steps.
 *
 * A goal with no milestones is a wish, so wherever the user writes a goal we can already show the
 * steps their category coach would open with, and they fill in, swap, or ignore them. Pure, local
 * and deterministic: the same title always yields the same steps, instantly and offline (no AI call,
 * so it costs nothing and works in the guest/no-network case).
 *
 * Two sources, in order:
 *  1. the closest [GoalTemplateProvider] template, matched on words shared with the title/description
 *     (that library already holds hand-written, chronological steps per goal shape), and
 *  2. a category arc, a five-beat "name it → first move → prove it → check → land it" shape phrased
 *     in that category's voice, with the user's own words folded back in.
 */
object MilestoneCoach {

    /** Words that carry no signal when matching a title against the template library. */
    private val STOPWORDS = setOf(
        "the", "and", "for", "with", "get", "got", "have", "has", "own", "one", "out", "off", "per",
        "into", "from", "that", "this", "your", "you", "our", "his", "her", "its", "how", "why",
        "what", "when", "who", "all", "any", "each", "more", "most", "some", "such", "than", "then",
        "them", "they", "will", "want", "wants", "need", "needs", "make", "made", "start", "starting",
        "begin", "new", "next", "every", "day", "days", "week", "weeks", "month", "months", "year",
        "years", "goal", "goals", "plan", "plans",
    )

    /** How many suggestions a caller gets by default. Five reads as a plan; ten reads as a chore. */
    private const val DEFAULT_LIMIT = 5

    /**
     * The steps the [category] coach would put on the board for a goal called [title].
     *
     * [existingTitles] (the milestones the goal already has) are filtered out so the card never
     * suggests a step the user already wrote. Returns at most [limit] steps, never empty for a
     * non-blank title.
     */
    fun suggest(
        title: String,
        category: GoalCategory,
        description: String = "",
        existingTitles: Collection<String> = emptyList(),
        limit: Int = DEFAULT_LIMIT,
    ): List<String> {
        if (title.isBlank()) return emptyList()
        val taken = existingTitles.map { it.normalizedForCompare() }.toSet()
        val fromTemplate = matchTemplate(title, description, category)?.suggestedMilestones.orEmpty()
        val steps = if (fromTemplate.isNotEmpty()) fromTemplate else categoryArc(title, category)
        return steps
            .filter { it.normalizedForCompare() !in taken }
            .distinctBy { it.normalizedForCompare() }
            .take(limit)
    }

    /** One line of framing above the suggestions, in the shape of "here's how I'd start". */
    fun opener(coachName: String, category: GoalCategory): String = when (category) {
        GoalCategory.CAREER -> "$coachName would break this into moves you can point at in a review."
        GoalCategory.MONEY -> "$coachName would make each step a number you can check."
        GoalCategory.BODY -> "$coachName would start smaller than feels impressive, then build."
        GoalCategory.PEOPLE -> "$coachName would turn this into specific people and specific dates."
        GoalCategory.WELLBEING -> "$coachName would keep the steps gentle and repeatable."
        GoalCategory.PURPOSE -> "$coachName would start with why, then make it visible."
        GoalCategory.FAMILY -> "$coachName would protect the time first, then fill it."
    }

    // ── Template matching ─────────────────────────────────────────────────────

    /**
     * The template whose title/tags share the most meaningful words with what the user wrote. Same
     * category first (a "run a marathon" goal should not borrow a budgeting template), then the whole
     * library, so a well-named goal filed under the wrong category still gets real steps.
     */
    private fun matchTemplate(title: String, description: String, category: GoalCategory): GoalTemplate? {
        val words = ("$title $description").meaningfulWords()
        if (words.isEmpty()) return null
        val inCategory = GoalTemplateProvider.getTemplatesByCategory(category)
        return bestMatch(words, inCategory) ?: bestMatch(words, GoalTemplateProvider.getAllTemplates())
    }

    private fun bestMatch(words: Set<String>, templates: List<GoalTemplate>): GoalTemplate? =
        templates
            .map { it to it.matchScore(words) }
            .filter { (_, score) -> score >= 2 } // one shared word is a coincidence, two is a topic
            .maxByOrNull { (_, score) -> score }
            ?.first

    private fun GoalTemplate.matchScore(words: Set<String>): Int {
        val titleWords = title.meaningfulWords()
        val tagWords = tags.joinToString(" ").meaningfulWords()
        // A title hit is worth more than a tag hit: tags are broad ("health"), titles are the goal.
        return words.count { it in titleWords } * 2 + words.count { it in tagWords }
    }

    // ── Category arc (the fallback) ───────────────────────────────────────────

    /**
     * A five-beat plan phrased for [category], with the user's own words ([title], shortened) folded
     * back in so the first step reads as being about *their* goal, not about goals in general.
     */
    private fun categoryArc(title: String, category: GoalCategory): List<String> {
        val subject = title.shortSubject()
        return when (category) {
            GoalCategory.CAREER -> listOf(
                "Write down what \"$subject\" looks like when it's done",
                "Find one person who has already done it and ask them how",
                "Book the first two hours of real work this week",
                "Ship something small others can see",
                "Review what moved and decide the next push",
            )
            GoalCategory.MONEY -> listOf(
                "Put a number and a date on \"$subject\"",
                "Check where the money goes today for one week",
                "Automate the first transfer, however small",
                "Cut or add one thing that changes the number",
                "Check the balance against the plan",
            )
            GoalCategory.BODY -> listOf(
                "Decide what \"$subject\" means in one measurable line",
                "Do the smallest version of it three times this week",
                "Set the time and place it happens by default",
                "Add one notch of difficulty once it feels easy",
                "Measure again and compare with week one",
            )
            GoalCategory.PEOPLE -> listOf(
                "Name the people \"$subject\" is really about",
                "Reach out to the first one this week",
                "Put a recurring time in the calendar",
                "Do one thing that asks nothing back",
                "Look back at who you actually saw this month",
            )
            GoalCategory.WELLBEING -> listOf(
                "Name what you want to feel more of in \"$subject\"",
                "Pick the one practice you'll actually repeat",
                "Attach it to something you already do daily",
                "Notice what makes the hard days harder",
                "Look back at a month of it and keep what worked",
            )
            GoalCategory.PURPOSE -> listOf(
                "Write one paragraph on why \"$subject\" matters to you",
                "Choose the first act that makes it real",
                "Give it a standing slot in your week",
                "Share it with one person who will ask about it",
                "Reread the paragraph and adjust the path",
            )
            GoalCategory.FAMILY -> listOf(
                "Agree with them on what \"$subject\" means",
                "Protect the time in the calendar first",
                "Plan the first one and make it happen",
                "Make it a ritual, not a one-off",
                "Ask them what they'd keep and what they'd change",
            )
        }
    }

    // ── Text helpers ──────────────────────────────────────────────────────────

    /** A short, quotable version of the goal title for use inside a step. */
    private fun String.shortSubject(): String {
        val cleaned = trim().trimEnd('.', '!', '?')
        if (cleaned.length <= 42) return cleaned
        return cleaned.take(39).trimEnd().trimEnd(',') + "…"
    }

    private fun String.meaningfulWords(): Set<String> =
        lowercase()
            .map { if (it.isLetterOrDigit()) it else ' ' }
            .joinToString("")
            .split(' ')
            .filter { it.length >= 3 && it !in STOPWORDS }
            .toSet()

    private fun String.normalizedForCompare(): String =
        lowercase().filter { it.isLetterOrDigit() }
}
