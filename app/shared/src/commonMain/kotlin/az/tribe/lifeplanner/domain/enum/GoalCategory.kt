package az.tribe.lifeplanner.domain.enum

enum class GoalCategory(val order: Int) {
    CAREER(1),
    MONEY(2),
    BODY(3),
    PEOPLE(4),
    WELLBEING(5),
    PURPOSE(6),
    FAMILY(7);

    companion object {
        fun getAllSorted(): List<GoalCategory> {
            return entries.sortedBy { it.order }
        }
    }
}

