package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.IdentityStatement
import kotlinx.coroutines.flow.Flow

interface IdentityStatementRepository {
    fun observeAll(): Flow<List<IdentityStatement>>
    suspend fun getAll(): List<IdentityStatement>
    suspend fun getById(id: String): IdentityStatement?
    suspend fun getByValue(valueId: String): List<IdentityStatement>
    suspend fun insert(statement: IdentityStatement)
    suspend fun update(statement: IdentityStatement)
    suspend fun deleteById(id: String)
}
