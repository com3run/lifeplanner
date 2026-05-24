package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.data.mapper.toDomainLifeValues
import az.tribe.lifeplanner.data.mapper.toEntity
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.deleteLifeValueById
import az.tribe.lifeplanner.infrastructure.getActiveLifeValues
import az.tribe.lifeplanner.infrastructure.getAllLifeValues
import az.tribe.lifeplanner.infrastructure.getLifeValueById
import az.tribe.lifeplanner.infrastructure.insertLifeValue
import az.tribe.lifeplanner.infrastructure.insertLifeValues
import az.tribe.lifeplanner.infrastructure.observeAllLifeValues
import az.tribe.lifeplanner.infrastructure.updateLifeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LifeValueRepositoryImpl(
    private val db: SharedDatabase,
    private val syncManager: SyncManager
) : LifeValueRepository {

    override fun observeAllLifeValues(): Flow<List<LifeValue>> =
        db.observeAllLifeValues().map { it.toDomainLifeValues() }

    override suspend fun getAllLifeValues(): List<LifeValue> =
        db.getAllLifeValues().toDomainLifeValues()

    override suspend fun getActiveLifeValues(): List<LifeValue> =
        db.getActiveLifeValues().toDomainLifeValues()

    override suspend fun getLifeValueById(id: String): LifeValue? =
        db.getLifeValueById(id)?.toDomain()

    override suspend fun insertLifeValue(value: LifeValue) {
        db.insertLifeValue(value.toEntity())
        syncManager.requestSync()
    }

    override suspend fun insertLifeValues(values: List<LifeValue>) {
        db.insertLifeValues(values.map { it.toEntity() })
        syncManager.requestSync()
    }

    override suspend fun updateLifeValue(value: LifeValue) {
        db.updateLifeValue(value.toEntity())
        syncManager.requestSync()
    }

    override suspend fun deleteLifeValueById(id: String) {
        db.deleteLifeValueById(id)
        syncManager.requestSync()
    }
}
