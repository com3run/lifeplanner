package az.tribe.lifeplanner.ui.goal

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserSituationRepository

internal fun detectCategoryFromText(text: String): GoalCategory {
    val lower = text.lowercase()
    return when {
        Regex("\\b(job|career|work|promotion|salary|professional|skills?|coding|programming|developer|business|startup|freelance|resume|interview|linkedin)\\b").containsMatchIn(lower) -> GoalCategory.CAREER
        Regex("\\b(money|invest|save|saving|debt|budget|financial|income|wealth|crypto|stock|fund|retire|afford|expensive|house|mortgage)\\b").containsMatchIn(lower) -> GoalCategory.MONEY
        Regex("\\b(run|gym|workout|exercise|weight|health|fitness|sport|marathon|diet|nutrition|body|muscle|swim|cycling|fat|sleep|calories)\\b").containsMatchIn(lower) -> GoalCategory.BODY
        Regex("\\b(friend|social|network|relationship|meet|community|connect|dating|shy|introvert|people|talk|communication)\\b").containsMatchIn(lower) -> GoalCategory.PEOPLE
        Regex("\\b(mental|emotional|anxiety|stress|mindset|happiness|confident|therapy|wellbeing|mood|feelings?|heal|trauma|self.esteem)\\b").containsMatchIn(lower) -> GoalCategory.WELLBEING
        Regex("\\b(meditat|spiritual|mindful|peace|gratitude|purpose|meaning|soul|prayer|faith|church|mosque|zen|inner)\\b").containsMatchIn(lower) -> GoalCategory.PURPOSE
        Regex("\\b(family|parent|child|kids?|spouse|partner|husband|wife|marriage|home|sibling|mom|dad|grandp)\\b").containsMatchIn(lower) -> GoalCategory.FAMILY
        else -> GoalCategory.WELLBEING
    }
}

internal fun buildCouncilNotes(
    situation: UserSituation?,
    option: GoalOption,
    category: GoalCategory
): List<Pair<String, String>> {
    if (situation == null) return emptyList()
    val notes = mutableListOf<Pair<String, String>>()
    val body = situation.body
    val meta = situation.meta
    val lowerTitle = option.title.lowercase()

    if ((body.sleepHours != null && body.sleepHours < 6f) || (body.energyRating != null && body.energyRating < 5)) {
        notes.add("Kai 💪" to "Your energy and sleep are limited right now. I'd start at 60% of whatever pace feels right — recovery is how you win long-term.")
    }
    if (category == GoalCategory.CAREER && (meta.stressLevel != null && meta.stressLevel >= 7)) {
        notes.add("Luna ✨" to "Your stress is high. Let's pace this goal so it doesn't add to your load — sustainable > aggressive.")
    }
    if (category != GoalCategory.MONEY && (lowerTitle.contains("promot") || lowerTitle.contains("job") || lowerTitle.contains("career") || lowerTitle.contains("salary"))) {
        notes.add("Morgan 💰" to "A career move often comes with a pay jump. Want me to open a parallel money goal to capture that?")
    }
    if (lowerTitle.contains("network") || lowerTitle.contains("speak") || lowerTitle.contains("outreach") || lowerTitle.contains("connect")) {
        notes.add("Sam 🤝" to "This goal needs people. I can help you build a relationship strategy that doesn't feel forced.")
    }
    if (situation.purpose.topValues.isNotEmpty() && meta.stressLevel != null && meta.stressLevel >= 8) {
        notes.add("River 🧘" to "High stress + an ambitious goal is a tricky combo. Check your values: does this goal feed or drain you?")
    }
    return notes.take(2)
}

internal suspend fun persistGoalSelectionMemory(
    option: GoalOption,
    userId: String,
    situation: UserSituation,
    repo: UserSituationRepository
): UserSituation {
    repo.updateMeta(userId, situation.meta.copy(
        topPriority = option.category,
        confidence = maxOf(situation.meta.confidence, 0.3f)
    ))
    when (option.category) {
        GoalCategory.CAREER -> if (situation.career.careerGoal == null) repo.updateCareer(
            userId, situation.career.copy(careerGoal = option.title, confidence = maxOf(situation.career.confidence, 0.4f))
        )
        GoalCategory.MONEY -> if (situation.money.financialGoal == null) repo.updateMoney(
            userId, situation.money.copy(financialGoal = option.title, confidence = maxOf(situation.money.confidence, 0.4f))
        )
        GoalCategory.PURPOSE -> if (situation.purpose.longTermVision == null) repo.updatePurpose(
            userId, situation.purpose.copy(longTermVision = option.title, confidence = maxOf(situation.purpose.confidence, 0.4f))
        )
        else -> Unit
    }
    return repo.getOrCreate(userId)
}
