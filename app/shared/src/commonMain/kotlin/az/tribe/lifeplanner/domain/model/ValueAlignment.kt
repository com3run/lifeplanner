package az.tribe.lifeplanner.domain.model

/**
 * Pillar 5, how much the user's actual choices have served a given [LifeValue]: the real
 * "becoming" progress signal (vs. raw XP). A completed goal serves the value it's tagged with;
 * a decision serves the value of its related goal.
 */
data class ValueAlignment(
    val valueId: String,
    val valueTitle: String,
    val completedGoalCount: Int,
    val decisionCount: Int,
    val goalShare: Double, // fraction of value-tagged completed goals that served this value (0..1)
)
