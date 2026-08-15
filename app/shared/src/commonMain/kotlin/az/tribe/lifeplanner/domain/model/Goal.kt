package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.enum.GoalTimeline
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class Goal(
    val id: String,
    val category: GoalCategory,
    val title: String,
    val description: String,
    val status: GoalStatus,
    val timeline: GoalTimeline,
    val dueDate: LocalDate,
    val progress: Long? = 0,
    val milestones: List<Milestone> = emptyList(),
    val notes: String = "",
    val createdAt: LocalDateTime,
    val completionRate: Float = 0f, // For the 60% progress shown in UI
    val isArchived: Boolean = false,
    val aiReasoning: String? = null,
    val valueId: String? = null,
    /**
     * The Wheel of Life area this goal serves: the goal's "why", and what ties it to a score the
     * user gave us. Null only for goals saved before this existed; [GoalWheelAreaInferrer] fills it
     * in on the next save.
     */
    val wheelArea: WheelArea? = null,
    // Pillar 4 (Causal Model): the user's forecast captured at creation, kept separate from
    // the live, editable dueDate, so we can later calibrate predicted vs actual.
    val predictedDueDate: LocalDate? = null
)

