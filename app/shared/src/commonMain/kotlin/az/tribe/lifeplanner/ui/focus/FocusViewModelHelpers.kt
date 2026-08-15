package az.tribe.lifeplanner.ui.focus

import az.tribe.lifeplanner.domain.model.XpRewards
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

// ---------------------------------------------------------------------------
// Pure XP calculations, no ViewModel state access needed
// ---------------------------------------------------------------------------

internal fun calculateXpForDuration(minutes: Int): Int {
    return when {
        minutes >= 60 -> XpRewards.FOCUS_SESSION_60
        minutes >= 45 -> XpRewards.FOCUS_SESSION_45
        minutes >= 25 -> XpRewards.FOCUS_SESSION_25
        minutes >= 15 -> XpRewards.FOCUS_SESSION_15
        else -> (minutes * 0.5f).toInt().coerceAtLeast(1)
    }
}

internal fun calculatePartialXp(elapsedSeconds: Int): Int {
    val elapsedMinutes = elapsedSeconds / 60
    return if (elapsedMinutes >= 5) {
        (elapsedMinutes * 0.5f).toInt()
    } else {
        0
    }
}

// ---------------------------------------------------------------------------
// Extension functions, ViewModel helpers that load / refresh state
// ---------------------------------------------------------------------------

internal fun FocusViewModel.loadTodayStats() {
    viewModelScope.launch {
        try {
            val todaySessions = focusRepository.getTodaySessions()
            _todaySessionCount.value = todaySessions.count { it.wasCompleted }
            _todaySeconds.value = todaySessions
                .filter { it.wasCompleted }
                .sumOf { it.actualDurationSeconds }
        } catch (e: Exception) { Logger.e("FocusViewModel") { "loadTodayStats failed: ${e.message}" } }
    }
}

internal fun FocusViewModel.loadAllTimeStats() {
    viewModelScope.launch {
        try {
            _allTimeSessionCount.value = focusRepository.getTotalSessionCount()
            _allTimeSeconds.value = focusRepository.getTotalFocusSeconds().toInt()
        } catch (e: Exception) { Logger.e("FocusViewModel") { "loadAllTimeStats failed: ${e.message}" } }
    }
}

internal fun FocusViewModel.loadMilestoneFocusMinutes(milestoneId: String) {
    viewModelScope.launch {
        try {
            val sessions = focusRepository.getSessionsByMilestoneId(milestoneId)
            _milestoneFocusMinutes.value = sessions.sumOf { it.actualDurationSeconds } / 60
        } catch (e: Exception) {
            Logger.e("FocusViewModel") { "loadMilestoneFocusMinutes failed: ${e.message}" }
        }
    }
}

internal fun FocusViewModel.checkCompletionEligibility() {
    val milestoneId = _selectedMilestone.value?.id ?: return
    viewModelScope.launch {
        try {
            val sessions = focusRepository.getSessionsByMilestoneId(milestoneId)
            val completedSessions = sessions.filter { it.wasCompleted }
            val userProgress = gamificationRepository.getUserProgress().firstOrNull()
            val level = userProgress?.currentLevel ?: 1
            // Level 5+ can always complete; below level 5 need at least 1 completed session
            _canCompleteMilestone.value = level >= 5 || completedSessions.isNotEmpty()
        } catch (e: Exception) {
            Logger.e("FocusViewModel") { "checkCompletionEligibility failed: ${e.message}" }
            _canCompleteMilestone.value = true
        }
    }
}
