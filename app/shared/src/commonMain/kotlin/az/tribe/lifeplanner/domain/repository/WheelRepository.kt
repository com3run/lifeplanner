package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.ComparisonPeriod
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelComparison
import az.tribe.lifeplanner.domain.model.WheelReport
import az.tribe.lifeplanner.domain.model.WheelSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

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

    /**
     * Records today's wheel, replacing any snapshot already taken today.
     *
     * Called on open. A past wheel cannot be recomputed, since predictions read trailing windows
     * that age out, so a day that goes uncaptured is a day of history lost for good.
     */
    suspend fun captureSnapshot()

    /**
     * Today's wheel against the nearest snapshot at or before [period] ago.
     *
     * Null when there is nothing far enough back to compare against, which is the normal state for
     * a new user rather than an error.
     */
    suspend fun compareTo(period: ComparisonPeriod): WheelComparison?

    /** Today's wheel against the nearest snapshot at or before [date]. */
    suspend fun compareToDate(date: LocalDate): WheelComparison?

    suspend fun snapshots(): List<WheelSnapshot>
}
