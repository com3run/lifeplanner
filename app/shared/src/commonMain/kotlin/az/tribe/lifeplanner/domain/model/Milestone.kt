package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDate

data class Milestone(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: LocalDate? = null,
    // Pillar 4 (Causal Model): estimated effort in minutes, captured at creation.
    val estimatedEffort: Int? = null
)