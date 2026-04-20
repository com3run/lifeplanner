package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import az.tribe.lifeplanner.database.UserSituationEntity
import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.UserSituation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private val situationJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

suspend fun SharedDatabase.upsertUserSituation(
    id: String,
    metaJson: String,
    careerJson: String,
    moneyJson: String,
    bodyJson: String,
    peopleJson: String,
    purposeJson: String,
    lastUpdatedBy: String
) {
    this { db ->
        db.lifePlannerDBQueries.upsertUserSituation(
            id = id,
            meta_json = metaJson,
            career_json = careerJson,
            money_json = moneyJson,
            body_json = bodyJson,
            people_json = peopleJson,
            purpose_json = purposeJson,
            last_updated_by = lastUpdatedBy,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.getUserSituation(): UserSituationEntity? {
    return this { db -> db.lifePlannerDBQueries.getUserSituation().executeAsOneOrNull() }
}

suspend fun SharedDatabase.getCurrentUserId(): String? {
    return this { db -> db.lifePlannerDBQueries.getCurrentUser().executeAsOneOrNull() }?.id
}

fun UserSituationEntity.toUserSituationDomain(): UserSituation = UserSituation(
    meta = try { situationJson.decodeFromString<MetaSlice>(meta_json) } catch (_: Exception) { MetaSlice() },
    career = try { situationJson.decodeFromString<CareerSlice>(career_json) } catch (_: Exception) { CareerSlice() },
    money = try { situationJson.decodeFromString<MoneySlice>(money_json) } catch (_: Exception) { MoneySlice() },
    body = try { situationJson.decodeFromString<BodySlice>(body_json) } catch (_: Exception) { BodySlice() },
    people = try { situationJson.decodeFromString<PeopleSlice>(people_json) } catch (_: Exception) { PeopleSlice() },
    purpose = try { situationJson.decodeFromString<PurposeSlice>(purpose_json) } catch (_: Exception) { PurposeSlice() },
    lastUpdatedBy = last_updated_by,
    updatedAt = sync_updated_at ?: ""
)

fun SharedDatabase.observeUserSituation(): Flow<UserSituationEntity?> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.observeUserSituation()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    )
}
