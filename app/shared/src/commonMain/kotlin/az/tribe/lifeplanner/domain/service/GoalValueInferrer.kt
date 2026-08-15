package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.LifeValue

/**
 * Pillar 1, "Why-Chain" without the busywork. Picking the life value a goal serves used to be a
 * manual wizard step; most people skipped it or picked at random. This infers the best-fitting
 * existing [LifeValue] for a goal from its category and text, so the link gets set for free.
 *
 * Deliberately conservative: values are free-text (from onboarding), so a wrong "why" is worse than
 * none. Returns null unless there is a real signal (a value word appears in the goal, or the value
 * clearly belongs to the goal's category). The user can always override.
 *
 * Pure and platform-free so it is trivially unit-testable and usable from any layer.
 */
object GoalValueInferrer {

    /** @return the id of the best-matching active value, or null when nothing scores above zero. */
    fun infer(
        category: GoalCategory,
        title: String,
        description: String,
        values: List<LifeValue>,
    ): String? {
        val active = values.filter { it.isActive }
        if (active.isEmpty()) return null

        val goalText = "$title $description".lowercase()
        val goalTokens = tokenize(goalText)
        val categoryKeywords = keywordsFor(category)

        val scored = active.map { value ->
            val valueTokens = tokenize("${value.title} ${value.description}")
                .filter { it.length >= 3 }
            var score = 0

            // Strongest signal: a word from the value literally appears in the goal.
            for (t in valueTokens) if (goalTokens.contains(t)) score += 3

            // Category affinity: the value's own words describe this life area.
            for (t in valueTokens) if (categoryKeywords.contains(t)) score += 2

            // Weak signal: a category keyword shows up in the goal text and the value
            // sits in that same area, nudging category-aligned values ahead of neutral ones.
            if (valueTokens.any { categoryKeywords.contains(it) } && categoryKeywords.any { goalText.contains(it) }) {
                score += 1
            }

            value to score
        }

        val top = scored.maxByOrNull { it.second } ?: return null
        if (top.second <= 0) return null
        // An ambiguous why is the user's call, not ours: only auto-pick a clear single winner.
        if (scored.count { it.second == top.second } > 1) return null
        return top.first.id
    }

    /**
     * The set of categories that already have a representative value, a value whose title is the
     * category name or whose words describe that life area. Used to seed only the *missing*
     * categories so every life area has a "why" to link to without duplicating what the user has.
     */
    fun coveredCategories(values: List<LifeValue>): Set<GoalCategory> {
        val active = values.filter { it.isActive }
        if (active.isEmpty()) return emptySet()
        return GoalCategory.entries.filter { category ->
            val keywords = keywordsFor(category)
            active.any { v ->
                v.title.equals(category.displayName, ignoreCase = true) ||
                    tokenize("${v.title} ${v.description}").any { it.length >= 3 && keywords.contains(it) }
            }
        }.toSet()
    }

    private fun tokenize(text: String): Set<String> =
        text.lowercase()
            .split(*SEPARATORS)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    /** Words that characterise each life area, used to match free-text values to a category. */
    private fun keywordsFor(category: GoalCategory): Set<String> = when (category) {
        GoalCategory.CAREER -> setOf("career", "work", "job", "professional", "business", "skill", "learn", "master", "craft", "impact", "ambition", "success")
        GoalCategory.MONEY -> setOf("money", "financial", "finance", "wealth", "save", "saving", "debt", "income", "invest", "security", "freedom", "abundance")
        GoalCategory.BODY -> setOf("health", "healthy", "fitness", "fit", "body", "exercise", "physical", "weight", "strength", "energy", "run", "gym", "sport", "vitality")
        GoalCategory.PEOPLE -> setOf("social", "friend", "friends", "friendship", "relationship", "relationships", "community", "people", "connect", "connection", "belonging", "love")
        GoalCategory.WELLBEING -> setOf("emotional", "wellbeing", "calm", "stress", "mind", "mental", "peace", "balance", "happy", "happiness", "joy", "contentment", "gratitude")
        GoalCategory.PURPOSE -> setOf("purpose", "spiritual", "meaning", "growth", "grow", "faith", "meditation", "meditate", "mindful", "mindfulness", "wisdom", "authenticity", "contribution")
        GoalCategory.FAMILY -> setOf("family", "kids", "children", "child", "parent", "parenting", "partner", "spouse", "home", "marriage", "legacy")
    }

    private val SEPARATORS = charArrayOf(' ', ',', '.', '!', '?', ';', ':', '-', '/', '(', ')', '"', '\'', '\n', '\t')
}
