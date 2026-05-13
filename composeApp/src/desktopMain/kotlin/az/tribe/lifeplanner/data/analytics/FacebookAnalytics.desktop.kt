package az.tribe.lifeplanner.data.analytics

actual object FacebookAnalytics {
    actual fun logCompleteRegistration(method: String) {}
    actual fun logCompleteTutorial() {}
    actual fun logViewContent(contentId: String, contentType: String) {}
    actual fun logAchieveLevel(level: Int) {}
    actual fun logUnlockAchievement(description: String) {}
    actual fun logSearch(query: String, contentType: String) {}
    actual fun logSessionStart() {}
    actual fun logGoalCreated(category: String) {}
    actual fun logCoachChatStarted(coachId: String) {}
    actual fun logHabitCheckedIn() {}
}
