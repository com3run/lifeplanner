package az.tribe.lifeplanner.domain

import kotlinx.datetime.LocalDate

data class Goal(
    val id: String,
    val category: GoalCategory,
    val title: String,
    val description: String,
    val status: GoalStatus,
    val timeline: GoalTimeline,
    val dueDate: LocalDate,
    val steps: List<Step> = emptyList()
)