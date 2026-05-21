package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.LifeValue
import kotlinx.coroutines.flow.Flow

interface LifeValueRepository {
    fun observeAllLifeValues(): Flow<List<LifeValue>>
    suspend fun getAllLifeValues(): List<LifeValue>
    suspend fun getActiveLifeValues(): List<LifeValue>
    suspend fun getLifeValueById(id: String): LifeValue?
    suspend fun insertLifeValue(value: LifeValue)
    suspend fun insertLifeValues(values: List<LifeValue>)
    suspend fun updateLifeValue(value: LifeValue)
    suspend fun deleteLifeValueById(id: String)
}
