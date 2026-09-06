package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.domain.model.DataError
import az.tribe.lifeplanner.data.model.GeminiResponseDto
import az.tribe.lifeplanner.domain.model.Result
import az.tribe.lifeplanner.data.model.UserQuestionnaireAnswers

interface GeminiService {
    suspend fun generateQuestions(userPrompt: String): Result<GeminiResponseDto, DataError.Network>
    suspend fun generateGoalsFromAnswers(
        originalPrompt: String,
        userAnswers: UserQuestionnaireAnswers,
    ): Result<GeminiResponseDto, DataError.Network>
    suspend fun generateGoalsDirect(prompt: String): Result<GeminiResponseDto, DataError.Network>
}