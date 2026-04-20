package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.model.BalanceInsight
import az.tribe.lifeplanner.domain.model.BalanceRecommendation
import az.tribe.lifeplanner.domain.model.BalanceRecommendationAction
import az.tribe.lifeplanner.domain.model.InsightPriority
import az.tribe.lifeplanner.domain.model.LifeArea
import kotlinx.serialization.Serializable

// ─── Cache DTOs ───────────────────────────────────────────────────────────────

@Serializable
internal data class CachedBalanceData(
    val insights: List<CachedInsight> = emptyList(),
    val recommendations: List<CachedRecommendation> = emptyList()
)

@Serializable
internal data class CachedInsight(
    val title: String,
    val description: String,
    val relatedAreaNames: List<String> = emptyList(),
    val priorityName: String
) {
    fun toBalanceInsight() = BalanceInsight(
        title = title,
        description = description,
        relatedAreas = relatedAreaNames.mapNotNull { name ->
            try { LifeArea.valueOf(name) } catch (_: Exception) { null }
        },
        priority = try { InsightPriority.valueOf(priorityName) } catch (_: Exception) { InsightPriority.LOW }
    )

    companion object {
        fun from(insight: BalanceInsight) = CachedInsight(
            title = insight.title,
            description = insight.description,
            relatedAreaNames = insight.relatedAreas.map { it.name },
            priorityName = insight.priority.name
        )
    }
}

@Serializable
internal data class CachedRecommendation(
    val title: String,
    val description: String,
    val targetAreaName: String,
    val actionTypeName: String,
    val suggestedGoal: String? = null,
    val suggestedHabit: String? = null
) {
    fun toBalanceRecommendation() = BalanceRecommendation(
        title = title,
        description = description,
        targetArea = try { LifeArea.valueOf(targetAreaName) } catch (_: Exception) { LifeArea.CAREER },
        actionType = try { BalanceRecommendationAction.valueOf(actionTypeName) } catch (_: Exception) { BalanceRecommendationAction.INCREASE_FOCUS },
        suggestedGoal = suggestedGoal,
        suggestedHabit = suggestedHabit
    )

    companion object {
        fun from(rec: BalanceRecommendation) = CachedRecommendation(
            title = rec.title,
            description = rec.description,
            targetAreaName = rec.targetArea.name,
            actionTypeName = rec.actionType.name,
            suggestedGoal = rec.suggestedGoal,
            suggestedHabit = rec.suggestedHabit
        )
    }
}

// ─── AI Response DTOs ─────────────────────────────────────────────────────────

@Serializable
internal data class PreGeneratedGoalsResponse(
    val goals: List<PreGeneratedGoalData> = emptyList()
)

@Serializable
internal data class PreGeneratedGoalData(
    val area: String = "",
    val title: String = "",
    val description: String = "",
    val timeline: String = "SHORT_TERM",
    val milestones: List<String> = emptyList()
)
