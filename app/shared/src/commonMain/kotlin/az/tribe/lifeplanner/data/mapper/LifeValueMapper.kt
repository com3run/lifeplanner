package az.tribe.lifeplanner.data.mapper

import az.tribe.lifeplanner.database.LifeValueEntity
import az.tribe.lifeplanner.domain.model.LifeValue
import kotlin.time.Clock

fun LifeValueEntity.toDomain(): LifeValue = LifeValue(
    id = id,
    title = title,
    description = description,
    isActive = isActive == 1L,
    order = sortOrder.toInt()
)

fun LifeValue.toEntity(): LifeValueEntity = LifeValueEntity(
    id = id,
    title = title,
    description = description,
    isActive = if (isActive) 1L else 0L,
    sortOrder = order.toLong(),
    sync_updated_at = Clock.System.now().toString(),
    is_deleted = 0L,
    sync_version = 0L,
    last_synced_at = null
)

fun List<LifeValueEntity>.toDomainLifeValues(): List<LifeValue> = map { it.toDomain() }
