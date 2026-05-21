package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.UserSituation
import kotlinx.coroutines.flow.Flow

interface UserSituationRepository {
    fun observe(): Flow<UserSituation?>
    suspend fun getOrCreate(userId: String): UserSituation
    suspend fun upsert(userId: String, situation: UserSituation)
    suspend fun updateMeta(userId: String, meta: MetaSlice)
    suspend fun updateCareer(userId: String, career: CareerSlice)
    suspend fun updateMoney(userId: String, money: MoneySlice)
    suspend fun updateBody(userId: String, body: BodySlice)
    suspend fun updatePeople(userId: String, people: PeopleSlice)
    suspend fun updatePurpose(userId: String, purpose: PurposeSlice)
}
