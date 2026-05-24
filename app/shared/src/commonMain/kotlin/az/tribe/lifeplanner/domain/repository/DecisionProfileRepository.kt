package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.DecisionProfile
import kotlinx.coroutines.flow.Flow

/**
 * Pillar 7 — persistence for the user's single [DecisionProfile] (one row per user).
 * Inferred and rewritten by the TuningInferenceEngine; read by the Possibility ranking,
 * Choice Points, and the "Your Wiring" screen.
 */
interface DecisionProfileRepository {
    /** Emits the current profile, or `null` until one has been inferred/created. */
    fun observeProfile(): Flow<DecisionProfile?>

    /** The current profile, or `null` if none exists yet. */
    suspend fun getProfile(): DecisionProfile?

    /** Insert or replace the single profile row. */
    suspend fun upsertProfile(profile: DecisionProfile)
}
