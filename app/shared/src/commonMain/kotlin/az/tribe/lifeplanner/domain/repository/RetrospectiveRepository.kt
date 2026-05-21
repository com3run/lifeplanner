package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.DaySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RetrospectiveRepository {
    suspend fun getDaySnapshot(date: LocalDate): DaySnapshot
    fun observeWeeklySnapshots(): Flow<List<DaySnapshot>>
    suspend fun getDatesWithActivity(start: LocalDate, end: LocalDate): Set<LocalDate>
    suspend fun getDayRecap(date: LocalDate): String?
    suspend fun saveDayRecap(date: LocalDate, recap: String)
}
