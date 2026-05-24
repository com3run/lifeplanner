package az.tribe.lifeplanner.data.analytics

import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import az.tribe.lifeplanner.MainApplication

actual object FacebookAnalytics {
    // Null when the app context / SDK isn't initialized (e.g. host unit tests, early
    // lifecycle) so analytics calls become safe no-ops instead of crashing.
    private val logger: AppEventsLogger? by lazy {
        runCatching { AppEventsLogger.newLogger(MainApplication.appContext) }.getOrNull()
    }

    actual fun logCompleteRegistration(method: String) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, method)
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params)
    }

    actual fun logCompleteTutorial() {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_SUCCESS, "1")
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL, params)
    }

    actual fun logViewContent(contentId: String, contentType: String) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
            putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, params)
    }

    actual fun logAchieveLevel(level: Int) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_LEVEL, level.toString())
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, params)
    }

    actual fun logUnlockAchievement(description: String) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, description)
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT, params)
    }

    actual fun logSearch(query: String, contentType: String) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, query)
            putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
        }
        logger?.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, params)
    }

    actual fun logSessionStart() {
        logger?.logEvent("session_start")
    }

    actual fun logGoalCreated(category: String) {
        val params = Bundle().apply { putString("category", category) }
        logger?.logEvent("goal_created", params)
    }

    actual fun logCoachChatStarted(coachId: String) {
        val params = Bundle().apply { putString("coach_id", coachId) }
        logger?.logEvent("coach_chat_started", params)
    }

    actual fun logHabitCheckedIn() {
        logger?.logEvent("habit_checkin")
    }
}
