package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelReport
import kotlinx.coroutines.flow.Flow

/**
 * The Wheel of Life: predicted scores merged with whatever the user has set themselves.
 *
 * Only user-set scores are stored. Predictions are recomputed on every read so they track the
 * signals behind them, rather than a cached number outliving the week of steps that produced it.
 */
interface WheelRepository {

    /** The current wheel, re-emitted when the user changes a score. */
    fun observeWheel(): Flow<WheelReport>

    suspend fun getWheel(): WheelReport

    /** Records the user's own score for an area, replacing any prediction. Snapped to a half. */
    suspend fun setScore(area: WheelArea, score: Double, note: String? = null)

    /** Drops the user's score so the area goes back to being predicted. */
    suspend fun clearScore(area: WheelArea)
}
