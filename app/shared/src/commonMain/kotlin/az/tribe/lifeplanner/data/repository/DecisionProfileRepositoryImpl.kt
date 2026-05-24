package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.data.mapper.toEntity
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.repository.DecisionProfileRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.getDecisionProfile
import az.tribe.lifeplanner.infrastructure.observeDecisionProfile
import az.tribe.lifeplanner.infrastructure.upsertDecisionProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DecisionProfileRepositoryImpl(
    private val db: SharedDatabase,
    private val syncManager: SyncManager
) : DecisionProfileRepository {

    override fun observeProfile(): Flow<DecisionProfile?> =
        db.observeDecisionProfile().map { it?.toDomain() }

    override suspend fun getProfile(): DecisionProfile? =
        db.getDecisionProfile()?.toDomain()

    override suspend fun upsertProfile(profile: DecisionProfile) {
        db.upsertDecisionProfile(profile.toEntity())
        syncManager.requestSync()
    }
}
