package az.tribe.lifeplanner.domain.enum

/**
 * The 7 stable goal categories. Enum constant names are load-bearing (DB, sync, mappers) and must
 * NOT change. [displayName] is the single source of truth for user-facing labels, the canonical
 * spellings from `terminology.md` (Career / Financial / Physical / Social / Emotional / Spiritual /
 * Family). Do not derive labels from `name` (that yields "Money"/"Body"/etc., which are wrong).
 */
enum class GoalCategory(val order: Int, val displayName: String) {
    CAREER(1, "Career"),
    MONEY(2, "Financial"),
    BODY(3, "Physical"),
    PEOPLE(4, "Social"),
    WELLBEING(5, "Emotional"),
    PURPOSE(6, "Spiritual"),
    FAMILY(7, "Family");

    companion object {
        fun getAllSorted(): List<GoalCategory> {
            return entries.sortedBy { it.order }
        }
    }
}

