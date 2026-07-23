package az.tribe.lifeplanner.ui.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.analytics.FacebookAnalytics
import az.tribe.lifeplanner.domain.enum.BadgeType
import az.tribe.lifeplanner.domain.enum.ChallengeType
import az.tribe.lifeplanner.domain.model.Badge
import az.tribe.lifeplanner.domain.model.BadgeRequirements
import az.tribe.lifeplanner.domain.model.Challenge
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.data.sync.SyncState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class GamificationViewModel(
    private val gamificationRepository: GamificationRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _userProgress = MutableStateFlow<UserProgress?>(null)
    val userProgress: StateFlow<UserProgress?> = _userProgress.asStateFlow()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    private val _newBadges = MutableStateFlow<List<Badge>>(emptyList())
    val newBadges: StateFlow<List<Badge>> = _newBadges.asStateFlow()

    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> = _activeChallenges.asStateFlow()

    private val _completedChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val completedChallenges: StateFlow<List<Challenge>> = _completedChallenges.asStateFlow()

    private val _availableChallenges = MutableStateFlow<List<ChallengeType>>(emptyList())
    val availableChallenges: StateFlow<List<ChallengeType>> = _availableChallenges.asStateFlow()

    private val _gamificationEvents = MutableSharedFlow<GamificationEvent>()
    val gamificationEvents = _gamificationEvents.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Prevents concurrent loadAll() calls from racing. */
    private val loadMutex = Mutex()

    /**
     * False until the first load has recorded what the user already had. Without it the initial
     * snapshot reads as "everything was just earned" and the whole badge history is celebrated.
     */
    private var hasBadgeBaseline = false

    init {
        viewModelScope.launch {
            loadAll()
        }
        viewModelScope.launch {
            syncManager.syncStatus
                .map { it.state }
                .distinctUntilChanged()
                .filter { it == SyncState.SYNCED }
                .collect { loadAll() }
        }
    }

    private suspend fun loadAll() = loadMutex.withLock {
        _isLoading.value = true
        try {
            val previousProgress = _userProgress.value
            val previousBadges = _badges.value

            loadUserProgress()
            loadBadges()
            loadChallenges()
            checkDailyStreak()

            // Detect level-up from server-side changes
            val newProgress = _userProgress.value
            if (previousProgress != null && newProgress != null &&
                newProgress.currentLevel > previousProgress.currentLevel) {
                FacebookAnalytics.logAchieveLevel(newProgress.currentLevel)
                Analytics.levelUp(newProgress.currentLevel, newProgress.totalXp.toLong())
                _gamificationEvents.emit(
                    GamificationEvent.LevelUp(
                        newLevel = newProgress.currentLevel,
                        title = newProgress.title
                    )
                )
            }

            // Detect newly earned badges from server-side changes.
            //
            // Two things would otherwise mistake a user's badge history for a burst of wins:
            // the first load has no baseline to diff against, and the load right after sign-in
            // sees every previously earned badge arrive from the server at once. So we skip the
            // baseline load, and require the badge to have been earned just now.
            if (!hasBadgeBaseline) {
                hasBadgeBaseline = true
            } else {
                val previousBadgeTypes = previousBadges.map { it.type }.toSet()
                val justEarned = _badges.value
                    .filter { it.type !in previousBadgeTypes }
                    .filter { earnedWithinCelebrationWindow(it) }

                justEarned.forEach { badge ->
                    FacebookAnalytics.logUnlockAchievement(badge.type.displayName)
                    Analytics.badgeEarned(badge.type.name)
                }

                // One celebration per batch: the rarest badge, most recent breaking a tie.
                val headline = justEarned.maxWithOrNull(
                    compareBy({ BadgeRequirements.getSignificance(it.type) }, { it.earnedAt })
                )
                if (headline != null) {
                    _gamificationEvents.emit(
                        GamificationEvent.BadgeEarned(headline, alsoEarnedCount = justEarned.size - 1)
                    )
                }
            }
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.w("GamificationVM") { "loadAll failed: ${e.message}" }
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * A badge counts as "just earned" only if it was awarded within this window. Sign-in pulls
     * the full badge history down in one sync; those all carry old timestamps.
     */
    private fun earnedWithinCelebrationWindow(badge: Badge): Boolean = try {
        val tz = TimeZone.currentSystemDefault()
        val age = Clock.System.now() - badge.earnedAt.toInstant(tz)
        age >= Duration.ZERO && age <= BADGE_CELEBRATION_WINDOW
    } catch (e: Exception) {
        co.touchlab.kermit.Logger.w("GamificationVM") { "badge age check failed: ${e.message}" }
        false
    }

    fun refresh() {
        viewModelScope.launch {
            loadAll()
        }
    }

    /**
     * Reset all cached state and reload from DB (called on sign-out/sign-in to clear stale data).
     */
    fun resetState() {
        _userProgress.value = null
        _badges.value = emptyList()
        _newBadges.value = emptyList()
        _activeChallenges.value = emptyList()
        _completedChallenges.value = emptyList()
        _availableChallenges.value = emptyList()
        // The next load re-establishes the baseline; without this the signed-in user's whole
        // badge history would be celebrated on the load that follows.
        hasBadgeBaseline = false
        // Re-read from DB to pick up the cleared state
        viewModelScope.launch { loadAll() }
    }

    private suspend fun loadUserProgress() {
        gamificationRepository.getUserProgress().collect {
            _userProgress.value = it
        }
    }

    private suspend fun loadBadges() {
        _badges.value = gamificationRepository.getAllBadges()
        _newBadges.value = gamificationRepository.getNewBadges()
    }

    private suspend fun loadChallenges() {
        gamificationRepository.cleanupExpiredChallenges()
        _activeChallenges.value = gamificationRepository.getActiveChallenges()
        _completedChallenges.value = gamificationRepository.getCompletedChallenges()
        _availableChallenges.value = gamificationRepository.getAvailableChallenges()
    }

    fun checkDailyStreak() {
        viewModelScope.launch {
            val (newStreak, xpAwarded) = gamificationRepository.updateDailyStreakRemote()
            if (xpAwarded > 0) {
                _gamificationEvents.emit(GamificationEvent.StreakUpdated(newStreak))
            }
            // Reload progress after RPC updates server state
            loadUserProgress()
        }
    }

    fun markBadgeAsSeen(badgeId: String) {
        viewModelScope.launch {
            gamificationRepository.markBadgeAsSeen(badgeId)
            loadBadges()
        }
    }

    fun markAllBadgesAsSeen() {
        viewModelScope.launch {
            gamificationRepository.markAllBadgesAsSeen()
            loadBadges()
        }
    }

    fun startChallenge(type: ChallengeType) {
        viewModelScope.launch {
            val challenge = gamificationRepository.startChallenge(type)
            loadChallenges()
            _gamificationEvents.emit(GamificationEvent.ChallengeStarted(challenge))
        }
    }

    fun updateChallengeProgress(challengeId: String, progress: Int) {
        viewModelScope.launch {
            gamificationRepository.updateChallengeProgress(challengeId, progress)

            // Check if challenge is completed
            val completedChallenge = gamificationRepository.checkAndCompleteChallenge(challengeId)
            if (completedChallenge != null) {
                _gamificationEvents.emit(GamificationEvent.ChallengeCompleted(completedChallenge))
                loadUserProgress()
            }

            loadChallenges()
        }
    }

    /**
     * Get all badge types with their earned status
     */
    fun getAllBadgeTypesWithStatus(): List<Pair<BadgeType, Badge?>> {
        val earnedBadges = _badges.value.associateBy { it.type }
        return BadgeType.entries.map { type ->
            type to earnedBadges[type]
        }
    }

    companion object {
        /** How recently a badge must have been awarded to be worth a celebration. */
        val BADGE_CELEBRATION_WINDOW: Duration = 10.minutes
    }
}

sealed class GamificationEvent {
    data class StreakUpdated(val newStreak: Int) : GamificationEvent()
    /** [alsoEarnedCount] is how many further badges landed in the same batch but are not shown. */
    data class BadgeEarned(val badge: Badge, val alsoEarnedCount: Int = 0) : GamificationEvent()
    data class ChallengeStarted(val challenge: Challenge) : GamificationEvent()
    data class ChallengeCompleted(val challenge: Challenge) : GamificationEvent()
    data class LevelUp(val newLevel: Int, val title: String) : GamificationEvent()
    data class XpEarned(val amount: Int) : GamificationEvent()
}
