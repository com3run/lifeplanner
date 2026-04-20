package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.ActivityLevel
import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.CircleSize
import az.tribe.lifeplanner.domain.model.EmploymentStatus
import az.tribe.lifeplanner.domain.model.IncomeBand
import az.tribe.lifeplanner.domain.model.LifeStage
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.RelationshipStatus
import az.tribe.lifeplanner.domain.model.SavingsHabit
import az.tribe.lifeplanner.domain.model.SocialEnergy
import az.tribe.lifeplanner.domain.model.UserSituation
import az.tribe.lifeplanner.domain.repository.UserRepository
import az.tribe.lifeplanner.domain.repository.UserSituationRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OnboardingPhase {
    LUNA_INTRO,
    LUNA_NAME,
    LUNA_PRIORITY,
    LUNA_WELLBEING,
    SPECIALIST_INTRO,
    SPECIALIST_Q1,
    SPECIALIST_Q2,
    SPECIALIST_Q3,
    SPECIALIST_Q4,
    COMPLETE
}

class CoachOnboardingViewModel(
    private val userSituationRepository: UserSituationRepository,
    private val userRepository: UserRepository,
    private val settings: Settings
) : ViewModel() {

    private val _phase = MutableStateFlow(OnboardingPhase.LUNA_INTRO)
    val phase: StateFlow<OnboardingPhase> = _phase.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // ── Luna answers ──────────────────────────────────────────────────────────

    var userName by mutableStateOf("")
    var userAge by mutableStateOf<Int?>(null)
    var topPriority by mutableStateOf<GoalCategory?>(null)
    var stressLevel by mutableStateOf(5)
    var sleepQuality by mutableStateOf(7)

    // ── Career answers (Alex) ─────────────────────────────────────────────────

    var employmentStatus by mutableStateOf<EmploymentStatus?>(null)
    var jobRole by mutableStateOf("")
    var yearsExperience by mutableStateOf<Int?>(null)
    var careerGoal by mutableStateOf("")

    // ── Money answers (Morgan) ────────────────────────────────────────────────

    var incomeBand by mutableStateOf<IncomeBand?>(null)
    var savingsHabit by mutableStateOf<SavingsHabit?>(null)
    var hasDebt by mutableStateOf<Boolean?>(null)
    var financialGoal by mutableStateOf("")

    // ── Body answers (Kai) ────────────────────────────────────────────────────

    var activityLevel by mutableStateOf<ActivityLevel?>(null)
    var sleepHours by mutableStateOf(7f)
    var energyRating by mutableStateOf(6)

    // ── People answers (Sam) ──────────────────────────────────────────────────

    var socialEnergy by mutableStateOf<SocialEnergy?>(null)
    var closeCircleSize by mutableStateOf<CircleSize?>(null)
    var relationshipStatus by mutableStateOf<RelationshipStatus?>(null)

    // ── Purpose answers (River) ───────────────────────────────────────────────

    var topValues by mutableStateOf<List<String>>(emptyList())
    var mindfulnessPractice by mutableStateOf<Boolean?>(null)
    var longTermVision by mutableStateOf("")

    // ── Derived ───────────────────────────────────────────────────────────────

    val specialistCoachId: String
        get() = when (topPriority) {
            GoalCategory.CAREER -> "alex_career"
            GoalCategory.MONEY -> "morgan_finance"
            GoalCategory.BODY -> "kai_fitness"
            GoalCategory.PEOPLE -> "sam_social"
            GoalCategory.PURPOSE -> "river_wellness"
            else -> "luna_general"
        }

    private val specialistQuestionCount: Int
        get() = when (specialistCoachId) {
            "alex_career" -> 4
            "morgan_finance" -> 4
            "kai_fitness" -> 3
            "sam_social" -> 3
            "river_wellness" -> 3
            else -> 0
        }

    fun overallCompleteness(): Float {
        var filled = 0
        var total = 5
        if (userName.isNotBlank() || userAge != null) filled++
        if (topPriority != null) filled++
        filled++ // stress/sleep always answered
        when (specialistCoachId) {
            "alex_career" -> { total += 2; if (employmentStatus != null) filled++; if (careerGoal.isNotBlank()) filled++ }
            "morgan_finance" -> { total += 2; if (incomeBand != null) filled++; if (savingsHabit != null) filled++ }
            "kai_fitness" -> { total += 2; if (activityLevel != null) filled++; if (energyRating > 0) filled++ }
            "sam_social" -> { total += 2; if (socialEnergy != null) filled++; if (closeCircleSize != null) filled++ }
            "river_wellness" -> { total += 2; if (topValues.isNotEmpty()) filled++; if (longTermVision.isNotBlank()) filled++ }
        }
        return (filled.toFloat() / total).coerceIn(0f, 1f)
    }

    fun advance() {
        _phase.value = nextPhase(_phase.value)
    }

    private fun nextPhase(current: OnboardingPhase): OnboardingPhase = when (current) {
        OnboardingPhase.LUNA_INTRO -> OnboardingPhase.LUNA_NAME
        OnboardingPhase.LUNA_NAME -> OnboardingPhase.LUNA_PRIORITY
        OnboardingPhase.LUNA_PRIORITY -> OnboardingPhase.LUNA_WELLBEING
        OnboardingPhase.LUNA_WELLBEING -> OnboardingPhase.SPECIALIST_INTRO
        OnboardingPhase.SPECIALIST_INTRO -> OnboardingPhase.SPECIALIST_Q1
        OnboardingPhase.SPECIALIST_Q1 -> if (specialistQuestionCount >= 2) OnboardingPhase.SPECIALIST_Q2 else OnboardingPhase.COMPLETE
        OnboardingPhase.SPECIALIST_Q2 -> if (specialistQuestionCount >= 3) OnboardingPhase.SPECIALIST_Q3 else OnboardingPhase.COMPLETE
        OnboardingPhase.SPECIALIST_Q3 -> if (specialistQuestionCount >= 4) OnboardingPhase.SPECIALIST_Q4 else OnboardingPhase.COMPLETE
        OnboardingPhase.SPECIALIST_Q4 -> OnboardingPhase.COMPLETE
        OnboardingPhase.COMPLETE -> OnboardingPhase.COMPLETE
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val userId = userRepository.getCurrentUser()?.id
                if (userId != null) {
                    userSituationRepository.upsert(userId, buildSituation())
                }
                settings.putBoolean(COACH_ONBOARDING_KEY, true)
                onDone()
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun buildSituation(): UserSituation {
        val age = userAge ?: 0
        val lifeStage = when {
            age in 13..17 -> LifeStage.STUDENT
            age in 18..22 -> LifeStage.EARLY_CAREER
            age in 23..35 -> LifeStage.EARLY_CAREER
            age in 36..55 -> LifeStage.MID_CAREER
            age > 55 -> LifeStage.SENIOR
            else -> null
        }
        val meta = MetaSlice(
            name = userName.takeIf { it.isNotBlank() },
            age = userAge,
            lifeStage = lifeStage,
            topPriority = topPriority,
            stressLevel = stressLevel,
            sleepQuality = sleepQuality,
            confidence = 0.7f
        )
        val career = if (specialistCoachId == "alex_career") CareerSlice(
            status = employmentStatus,
            role = jobRole.takeIf { it.isNotBlank() },
            yearsExperience = yearsExperience,
            careerGoal = careerGoal.takeIf { it.isNotBlank() },
            confidence = if (employmentStatus != null) 0.7f else 0f
        ) else CareerSlice()
        val money = if (specialistCoachId == "morgan_finance") MoneySlice(
            incomeBand = incomeBand,
            savingsHabit = savingsHabit,
            hasDebt = hasDebt,
            financialGoal = financialGoal.takeIf { it.isNotBlank() },
            confidence = if (incomeBand != null) 0.7f else 0f
        ) else MoneySlice()
        val body = if (specialistCoachId == "kai_fitness") BodySlice(
            activityLevel = activityLevel,
            sleepHours = sleepHours,
            energyRating = energyRating,
            confidence = if (activityLevel != null) 0.7f else 0f
        ) else BodySlice()
        val people = if (specialistCoachId == "sam_social") PeopleSlice(
            socialEnergy = socialEnergy,
            closeCircleSize = closeCircleSize,
            relationshipStatus = relationshipStatus,
            confidence = if (socialEnergy != null) 0.7f else 0f
        ) else PeopleSlice()
        val purpose = if (specialistCoachId == "river_wellness") PurposeSlice(
            topValues = topValues,
            mindfulnessPractice = mindfulnessPractice,
            longTermVision = longTermVision.takeIf { it.isNotBlank() },
            confidence = if (topValues.isNotEmpty()) 0.7f else 0f
        ) else PurposeSlice()
        return UserSituation(
            meta = meta, career = career, money = money,
            body = body, people = people, purpose = purpose,
            lastUpdatedBy = "luna_general"
        )
    }

    companion object {
        const val COACH_ONBOARDING_KEY = "coach_onboarding_complete"

        fun isComplete(settings: Settings) = settings.getBoolean(COACH_ONBOARDING_KEY, false)
    }
}
