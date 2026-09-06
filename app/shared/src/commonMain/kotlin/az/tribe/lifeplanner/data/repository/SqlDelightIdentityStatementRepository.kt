package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.data.mapper.toDomainIdentityStatements
import az.tribe.lifeplanner.data.mapper.toEntity
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.IdentityStatement
import az.tribe.lifeplanner.domain.repository.IdentityStatementRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.deleteIdentityStatementById
import az.tribe.lifeplanner.infrastructure.getAllIdentityStatements
import az.tribe.lifeplanner.infrastructure.getIdentityStatementById
import az.tribe.lifeplanner.infrastructure.getIdentityStatementsByValue
import az.tribe.lifeplanner.infrastructure.insertIdentityStatement
import az.tribe.lifeplanner.infrastructure.observeAllIdentityStatements
import az.tribe.lifeplanner.infrastructure.updateIdentityStatement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightIdentityStatementRepository(
    private val db: SharedDatabase,
    private val syncManager: SyncManager
) : IdentityStatementRepository {

    override fun observeAll(): Flow<List<IdentityStatement>> =
        db.observeAllIdentityStatements().map { it.toDomainIdentityStatements() }

    override suspend fun getAll(): List<IdentityStatement> =
        db.getAllIdentityStatements().toDomainIdentityStatements()

    override suspend fun getById(id: String): IdentityStatement? =
        db.getIdentityStatementById(id)?.toDomain()

    override suspend fun getByValue(valueId: String): List<IdentityStatement> =
        db.getIdentityStatementsByValue(valueId).toDomainIdentityStatements()

    override suspend fun insert(statement: IdentityStatement) {
        db.insertIdentityStatement(statement.toEntity())
        syncManager.requestSync()
    }

    override suspend fun update(statement: IdentityStatement) {
        db.updateIdentityStatement(statement.toEntity())
        syncManager.requestSync()
    }

    override suspend fun deleteById(id: String) {
        db.deleteIdentityStatementById(id)
        syncManager.requestSync()
    }
}
