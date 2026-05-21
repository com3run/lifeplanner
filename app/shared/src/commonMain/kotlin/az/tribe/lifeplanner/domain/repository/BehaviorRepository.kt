package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.AppUsagePattern

interface BehaviorRepository {
    suspend fun recordScreenEnter(screen: String)
    suspend fun recordScreenExit(screen: String, durationMs: Long)
    suspend fun getPattern(): AppUsagePattern?
    suspend fun refreshPattern()
    suspend fun pruneOldEvents(keepDays: Int = 30)
}
