package az.tribe.lifeplanner.domain.model

import az.tribe.lifeplanner.domain.enum.GoalCategory
import kotlinx.serialization.Serializable

@Serializable
data class UserSituation(
    val meta: MetaSlice = MetaSlice(),
    val career: CareerSlice = CareerSlice(),
    val money: MoneySlice = MoneySlice(),
    val body: BodySlice = BodySlice(),
    val people: PeopleSlice = PeopleSlice(),
    val purpose: PurposeSlice = PurposeSlice(),
    val lastUpdatedBy: String = "",
    val updatedAt: String = ""
) {
    fun completeness(): Map<GoalCategory, Float> = mapOf(
        GoalCategory.WELLBEING to meta.confidence,
        GoalCategory.CAREER to career.confidence,
        GoalCategory.MONEY to money.confidence,
        GoalCategory.BODY to body.confidence,
        GoalCategory.PEOPLE to people.confidence,
        GoalCategory.PURPOSE to purpose.confidence
    )

    fun overallCompleteness(): Float = completeness().values.average().toFloat()
}

// ===== Enums =====

enum class LifeStage { STUDENT, EARLY_CAREER, FAMILY, MID_CAREER, SENIOR, RETIRED }

enum class EmploymentStatus { STUDENT, EMPLOYED, UNEMPLOYED, FREELANCE, ENTREPRENEUR, RETIRED }

enum class IncomeBand {
    UNDER_15K, BAND_15_30K, BAND_30_60K, BAND_60_100K, BAND_100_200K, OVER_200K
}

enum class SavingsHabit { NONE, SPORADIC, CONSISTENT, AGGRESSIVE }

enum class RiskAppetite { LOW, MEDIUM, HIGH }

enum class ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE }

enum class DietPattern { OMNIVORE, VEGETARIAN, VEGAN, KETO, INTERMITTENT_FASTING, OTHER }

enum class HealthFlag { CHRONIC_PAIN, ANXIETY, INSOMNIA, HIGH_STRESS, RECENT_INJURY }

enum class RelationshipStatus { SINGLE, IN_RELATIONSHIP, MARRIED, DIVORCED, WIDOWED }

enum class CircleSize { SOLO, SMALL, MEDIUM, LARGE }

enum class SocialEnergy { INTROVERT, AMBIVERT, EXTROVERT }

// ===== Slices =====

@Serializable
data class MetaSlice(
    val name: String? = null,
    val age: Int? = null,
    val lifeStage: LifeStage? = null,
    val topPriority: GoalCategory? = null,
    val overallMood: Int? = null,
    val stressLevel: Int? = null,
    val sleepQuality: Int? = null,
    val confidence: Float = 0f
)

@Serializable
data class CareerSlice(
    val status: EmploymentStatus? = null,
    val role: String? = null,
    val industry: String? = null,
    val yearsExperience: Int? = null,
    val topSkills: List<String> = emptyList(),
    val hasResume: Boolean = false,
    val resumeUrl: String? = null,
    val wantsResumeService: Boolean? = null,
    val careerGoal: String? = null,
    val confidence: Float = 0f
)

@Serializable
data class MoneySlice(
    val incomeBand: IncomeBand? = null,
    val currency: String? = null,
    val savingsHabit: SavingsHabit? = null,
    val hasDebt: Boolean? = null,
    val financialGoal: String? = null,
    val riskAppetite: RiskAppetite? = null,
    val confidence: Float = 0f
)

@Serializable
data class BodySlice(
    val activityLevel: ActivityLevel? = null,
    val sleepHours: Float? = null,
    val dietPattern: DietPattern? = null,
    val energyRating: Int? = null,
    val flags: List<HealthFlag> = emptyList(),
    val confidence: Float = 0f
)

@Serializable
data class PeopleSlice(
    val relationshipStatus: RelationshipStatus? = null,
    val closeCircleSize: CircleSize? = null,
    val familyContext: String? = null,
    val socialEnergy: SocialEnergy? = null,
    val confidence: Float = 0f
)

@Serializable
data class PurposeSlice(
    val topValues: List<String> = emptyList(),
    val mindfulnessPractice: Boolean? = null,
    val meaningSources: List<String> = emptyList(),
    val longTermVision: String? = null,
    val confidence: Float = 0f
)
