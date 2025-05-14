package az.tribe.lifeplanner.data

import az.tribe.lifeplanner.database.GoalEntity
import az.tribe.lifeplanner.domain.Goal
import az.tribe.lifeplanner.domain.GoalCategory
import az.tribe.lifeplanner.domain.GoalStatus
import az.tribe.lifeplanner.domain.GoalTimeline
import kotlinx.datetime.LocalDate


fun GoalEntity.toDomain(): Goal {
    return Goal(
        id = id,
        category = GoalCategory.valueOf(category),
        title = title,
        description = description,
        status = GoalStatus.valueOf(status),
        timeline = GoalTimeline.valueOf(timeline),
        dueDate = LocalDate.parse(dueDate),
        progress = progress,
        steps = emptyList() // Optional: implement if steps are persisted
    )
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        category = category.name,
        title = title,
        description = description,
        status = status.name,
        timeline = timeline.name,
        dueDate = dueDate.toString(),
        progress = progress?:0
    )
}