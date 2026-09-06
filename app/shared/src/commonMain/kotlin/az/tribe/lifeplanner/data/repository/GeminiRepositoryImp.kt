package az.tribe.lifeplanner.data.repository


import az.tribe.lifeplanner.data.mapper.getQuestionGenerationResponse
import az.tribe.lifeplanner.data.network.GeminiService
import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.domain.model.DataError
import az.tribe.lifeplanner.data.model.GoalTypeQuestions
import az.tribe.lifeplanner.domain.model.Result
import az.tribe.lifeplanner.data.model.UserQuestionnaireAnswers
import az.tribe.lifeplanner.domain.model.map
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.repository.GeminiRepository


class GeminiRepositoryImp(
    private val geminiService: GeminiService
) : GeminiRepository {


    // New implementation - questionnaire generation
    override suspend fun generateQuestionnaire(
        userPrompt: String
    ): Result<List<GoalTypeQuestions>, DataError.Network> {
        return geminiService.generateQuestions(userPrompt).map { response ->
            response.getQuestionGenerationResponse()?.goals ?: emptyList()
        }
    }

    // New implementation - personalized goals from answers
    override suspend fun generatePersonalizedGoals(
        originalPrompt: String,
        userAnswers: UserQuestionnaireAnswers
    ): Result<List<Goal>, DataError.Network> {
        return geminiService.generateGoalsFromAnswers(originalPrompt, userAnswers)
            .map { it.toDomain() }
    }

    override suspend fun generateGoalsDirect(prompt: String): Result<List<Goal>, DataError.Network> {
        return geminiService.generateGoalsDirect(prompt)
            .map { it.toDomain() }
    }
}