package az.tribe.lifeplanner.data.mapper

import az.tribe.lifeplanner.database.IdentityStatementEntity
import az.tribe.lifeplanner.domain.model.IdentityStatement
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

fun IdentityStatementEntity.toDomain(): IdentityStatement = IdentityStatement(
    id = id,
    statement = statement,
    valueId = valueId,
    isActive = isActive == 1L,
    sortOrder = sortOrder.toInt(),
    createdAt = LocalDateTime.parse(createdAt)
)

fun IdentityStatement.toEntity(): IdentityStatementEntity = IdentityStatementEntity(
    id = id,
    statement = statement,
    valueId = valueId,
    isActive = if (isActive) 1L else 0L,
    sortOrder = sortOrder.toLong(),
    createdAt = createdAt.toString(),
    sync_updated_at = Clock.System.now().toString(),
    is_deleted = 0L,
    sync_version = 0L,
    last_synced_at = null
)

fun List<IdentityStatementEntity>.toDomainIdentityStatements(): List<IdentityStatement> = map { it.toDomain() }
