package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.getUserSituation
import az.tribe.lifeplanner.infrastructure.observeUserSituation
import az.tribe.lifeplanner.infrastructure.upsertUserSituation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SqlDelightUserSituationRepository(
    private val database: SharedDatabase,
    private val syncManager: SyncManager
) : UserSituationRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun observe(): Flow<UserSituation?> {
        return database.observeUserSituation().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getOrCreate(userId: String): UserSituation {
        val existing = database.getUserSituation()
        if (existing != null) return existing.toDomain()
        val empty = UserSituation()
        upsertInternal(userId, empty)
        return empty
    }

    override suspend fun upsert(userId: String, situation: UserSituation) {
        upsertInternal(userId, situation)
        syncManager.requestSync()
    }

    override suspend fun updateMeta(userId: String, meta: MetaSlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(meta = meta, lastUpdatedBy = "luna_general"))
    }

    override suspend fun updateCareer(userId: String, career: CareerSlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(career = career, lastUpdatedBy = "alex_career"))
    }

    override suspend fun updateMoney(userId: String, money: MoneySlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(money = money, lastUpdatedBy = "morgan_finance"))
    }

    override suspend fun updateBody(userId: String, body: BodySlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(body = body, lastUpdatedBy = "kai_fitness"))
    }

    override suspend fun updatePeople(userId: String, people: PeopleSlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(people = people, lastUpdatedBy = "sam_social"))
    }

    override suspend fun updatePurpose(userId: String, purpose: PurposeSlice) {
        val current = getOrCreate(userId)
        upsert(userId, current.copy(purpose = purpose, lastUpdatedBy = "river_wellness"))
    }

    // ===== Private =====

    private suspend fun upsertInternal(userId: String, situation: UserSituation) {
        database.upsertUserSituation(
            id = userId,
            metaJson = json.encodeToString(situation.meta),
            careerJson = json.encodeToString(situation.career),
            moneyJson = json.encodeToString(situation.money),
            bodyJson = json.encodeToString(situation.body),
            peopleJson = json.encodeToString(situation.people),
            purposeJson = json.encodeToString(situation.purpose),
            lastUpdatedBy = situation.lastUpdatedBy
        )
    }

    private fun az.tribe.lifeplanner.database.UserSituationEntity.toDomain(): UserSituation {
        return UserSituation(
            meta = json.decodeFromString(meta_json),
            career = json.decodeFromString(career_json),
            money = json.decodeFromString(money_json),
            body = json.decodeFromString(body_json),
            people = json.decodeFromString(people_json),
            purpose = json.decodeFromString(purpose_json),
            lastUpdatedBy = last_updated_by,
            updatedAt = sync_updated_at ?: ""
        )
    }
}
