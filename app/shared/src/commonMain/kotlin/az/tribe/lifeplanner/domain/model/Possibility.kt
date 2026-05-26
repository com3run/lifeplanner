package az.tribe.lifeplanner.domain.model

/**
 * Pillar 6 (Free Agents), one divergent option generated in Possibility Mode. The book's frame:
 * creativity is "chance filtered by choice", so the AI's job is to *expand* the option set with
 * cognitive permutations, never to decide. The user converges afterward.
 *
 * Pure domain model: no platform or framework dependencies.
 */
data class Possibility(
    val id: String,
    /** The divergent option, phrased as something the user could actually try. */
    val text: String,
    /** Which cognitive permutation produced it, so the user sees the angle (not just the idea). */
    val permutation: PermutationKind,
    /** One line on why this might unstick things. Honest, not a hard sell. */
    val rationale: String,
)

/**
 * The cognitive permutations from *Free Agents* used to widen a stuck option set. Names are the
 * angle, never a judgement. The UI labels and the AI prompt both lean on these.
 */
enum class PermutationKind(val label: String) {
    /** Combine existing resources, habits, or goals in a new way. */
    RECOMBINE("Recombine"),

    /** Borrow a solution from a completely different domain. */
    ANALOGY("Analogy"),

    /** Challenge the assumption that is quietly limiting the options. */
    QUESTION_ASSUMPTION("Question the assumption"),

    /** Flip the problem: aim for the opposite, or remove instead of add. */
    INVERT("Invert"),

    /** Shrink it to the smallest possible version that still counts. */
    SHRINK("Shrink it");

    companion object {
        /** Lenient parse from the AI's string; falls back to RECOMBINE so a stray label never crashes. */
        fun fromString(value: String?): PermutationKind =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: RECOMBINE
    }
}
