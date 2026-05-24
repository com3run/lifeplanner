package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Pillar 5 — an "I'm becoming someone who…" statement, tied to a [LifeValue] (Pillar 1).
 * The identity layer that sits *above* XP/levels: who the user is choosing to become, not how
 * much activity they logged. Additive — XP/badges stay; this is the "becoming" signal.
 */
data class IdentityStatement(
    val id: String,
    val statement: String,
    val valueId: String? = null,   // → LifeValue (Pillar 1)
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: LocalDateTime
)
