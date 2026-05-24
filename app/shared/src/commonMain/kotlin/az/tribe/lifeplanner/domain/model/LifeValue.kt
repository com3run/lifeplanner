package az.tribe.lifeplanner.domain.model

/**
 * A first-class life value — the "reason" layer that sits above [Goal] in the
 * hierarchy (FocusSession → Milestone → Goal → LifeValue). 3–7 per user is typical.
 *
 * Pure domain model: no platform or framework dependencies.
 */
data class LifeValue(
    val id: String,
    val title: String,
    val description: String = "",
    val isActive: Boolean = true,
    val order: Int = 0
)
