package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.WheelArea

/**
 * Which area of the wheel a goal is actually about.
 *
 * This replaces [GoalValueInferrer] as the goal's "why". That one matched the goal's text against
 * the user's free-text onboarding values by shared words, which meant it was really matching
 * vocabulary rather than meaning, and it returned null on any tie — so most goals ended up with no
 * why at all, and the ones that got one got it for the wrong reason.
 *
 * The wheel areas are a fixed set of ten, and the goal already carries a [GoalCategory] the user
 * picked themselves. That makes the mapping a lookup rather than a guess, so this **always** returns
 * an area. Being always-set is the point: the tag is what connects a goal to a score the user gave
 * us, and a tag that is usually missing cannot connect anything.
 *
 * Where one category spans two areas the goal's own words break the tie, and the user can always
 * change it.
 */
object GoalWheelAreaInferrer {

    /**
     * Career splits: is this about the work mattering, or about getting better at something?
     * The wheel treats those as separate areas with separate rubrics, so a certification and a
     * promotion are not the same question.
     */
    private val GROWTH_WORDS = listOf(
        "learn", "course", "study", "skill", "certif", "degree", "read", "practice",
        "master", "improve", "training", "workshop", "language",
    )

    /**
     * Romance has no category of its own, because the goal categories predate the wheel and never
     * had one. Without this a "plan a weekend away with my partner" goal lands in Friends.
     */
    private val ROMANCE_WORDS = listOf(
        "partner", "girlfriend", "boyfriend", "spouse", "wife", "husband", "marriage",
        "married", "dating", "romance", "romantic", "relationship", "date night",
    )

    private val FAMILY_WORDS = listOf(
        "family", "mum", "mom", "dad", "mother", "father", "parents", "sister",
        "brother", "son", "daughter", "kids", "children", "grandma", "grandpa",
    )

    fun infer(category: GoalCategory, title: String, description: String = ""): WheelArea {
        val text = "$title $description".lowercase()

        return when (category) {
            // The two career areas. Mission is the default: most career goals are about the work
            // itself, and Growth is the narrower claim that needs evidence in the text.
            GoalCategory.CAREER ->
                if (GROWTH_WORDS.any { text.contains(it) }) WheelArea.GROWTH else WheelArea.MISSION

            // Social covers everyone who is not family, which includes a partner.
            GoalCategory.PEOPLE -> when {
                ROMANCE_WORDS.any { text.contains(it) } -> WheelArea.ROMANCE
                FAMILY_WORDS.any { text.contains(it) } -> WheelArea.FAMILY
                else -> WheelArea.FRIENDS
            }

            // A family goal about a partner is still about the partner. The reverse of the above.
            GoalCategory.FAMILY ->
                if (ROMANCE_WORDS.any { text.contains(it) }) WheelArea.ROMANCE else WheelArea.FAMILY

            GoalCategory.MONEY -> WheelArea.MONEY
            GoalCategory.BODY -> WheelArea.PHYSICAL
            GoalCategory.WELLBEING -> WheelArea.MENTAL
            GoalCategory.PURPOSE -> WheelArea.SPIRITUAL
        }
    }

    /**
     * The areas a user can pick from by hand.
     *
     * Joy is excluded on purpose, and for the same reason [WheelNudgePicker] never picks it: Joy is
     * a reading of the whole wheel rather than a slice with goals of its own. "Have more joy" is not
     * a goal, and tagging one to it would put a number on the wheel that no goal can move.
     */
    val selectable: List<WheelArea> = WheelArea.segments()
}
