package az.tribe.lifeplanner.ui.goal

import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.data.model.QuestionAnswer
import az.tribe.lifeplanner.data.model.UserQuestionnaireAnswers
import az.tribe.lifeplanner.data.model.onError
import az.tribe.lifeplanner.data.model.onSuccess
import az.tribe.lifeplanner.domain.model.Goal
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

// ---------------------------------------------------------------------------
// AI goal-generation extension functions for GoalViewModel.
// These live in a separate file to keep GoalViewModel.kt under 600 lines.
// All state fields and use-case properties they touch are declared `internal`
// in GoalViewModel so they are accessible here within the same module.
// ---------------------------------------------------------------------------

/**
 * Step 1: Generate questionnaire based on user prompt.
 * Example: generateQuestionnaire("I want to save money and get healthy")
 */
fun GoalViewModel.generateQuestionnaire(userPrompt: String) {
    viewModelScope.launch {
        try {
            _isLoadingQuestions.value = true
            _userPrompt.value = userPrompt
            _error.value = null

            generateAiQuestionnaireUseCase(userPrompt)
                .onSuccess { questions ->
                    _questions.value = questions
                    _questionnaireStep.value = QuestionnaireStep.ANSWERING
                }
                .onError { error ->
                    _error.value = "Failed to generate questions: ${error}"
                    _questionnaireStep.value = QuestionnaireStep.INPUT
                }
        } catch (e: Exception) {
            _error.value = "Failed to generate questions: ${e.message}"
            _questionnaireStep.value = QuestionnaireStep.INPUT
        } finally {
            _isLoadingQuestions.value = false
        }
    }
}

/**
 * Step 2: Answer a question.
 * Example: answerQuestion("What's your current fitness level?", "Beginner")
 */
fun GoalViewModel.answerQuestion(questionTitle: String, selectedOption: String) {
    val currentAnswers = _userAnswers.value.answers.toMutableList()

    // Find existing answer or add new one
    val existingIndex = currentAnswers.indexOfFirst { it.questionTitle == questionTitle }
    val newAnswer = QuestionAnswer(questionTitle, selectedOption)

    if (existingIndex >= 0) {
        currentAnswers[existingIndex] = newAnswer
    } else {
        currentAnswers.add(newAnswer)
    }

    _userAnswers.value = UserQuestionnaireAnswers(currentAnswers)
}

/**
 * Step 3: Generate personalized goals based on answers.
 */
fun GoalViewModel.generatePersonalizedGoals() {
    viewModelScope.launch {
        try {
            _isGeneratingPersonalizedGoals.value = true
            _error.value = null
            _questionnaireStep.value = QuestionnaireStep.GENERATING

            if (_userPrompt.value.isBlank()) {
                _error.value = "Original prompt is missing"
                return@launch
            }

            if (_userAnswers.value.answers.isEmpty()) {
                _error.value = "Please answer the questions first"
                return@launch
            }

            generateAiGoalsUseCase(
                _userPrompt.value,
                _userAnswers.value
            )
                .onSuccess { goals ->
                    // Store generated goals for display FIRST
                    _generatedGoalsFromAI.value = goals

                    // WAIT to add them to database until user clicks "Add Goal" buttons
                    // Don't automatically add them here

                    _questionnaireStep.value = QuestionnaireStep.RESULTS
                    Logger.d("GoalViewModel") { "Generated ${goals.size} personalized goals" }
                    goals.forEach { goal ->
                        Logger.d("GoalViewModel") { "Goal: ${goal.title}" }
                        goal.milestones.forEach { milestone ->
                            Logger.d("GoalViewModel") { "  - Milestone: ${milestone.title}" }
                        }
                    }
                }
                .onError { error ->
                    _error.value = "Failed to generate personalized goals: ${error}"
                    _questionnaireStep.value = QuestionnaireStep.ANSWERING
                }
        } catch (e: Exception) {
            _error.value = "Failed to generate personalized goals: ${e.message}"
            _questionnaireStep.value = QuestionnaireStep.ANSWERING
        } finally {
            _isGeneratingPersonalizedGoals.value = false
        }
    }
}

/**
 * AI-first: Generate goals directly from a prompt without questionnaire.
 */
fun GoalViewModel.generateGoalsDirectly(prompt: String) {
    viewModelScope.launch {
        try {
            _isGeneratingPersonalizedGoals.value = true
            _error.value = null
            _questionnaireStep.value = QuestionnaireStep.GENERATING

            Logger.d("GoalViewModel") { "AI Goal Generation: Starting direct generation with prompt: $prompt" }
            Analytics.aiGoalGenerationStarted()

            geminiRepository.generateGoalsDirect(prompt)
                .onSuccess { goals ->
                    Logger.d("GoalViewModel") { "AI Goal Generation: Received ${goals.size} goals" }
                    Analytics.aiGoalGenerationCompleted(goals.size)
                    if (goals.isEmpty()) {
                        _error.value = "AI returned no goals. Please try again."
                        _questionnaireStep.value = QuestionnaireStep.INPUT
                    } else {
                        _generatedGoalsFromAI.value = goals
                        goals.forEach { goal ->
                            Logger.d("GoalViewModel") { "  Goal: ${goal.title} (${goal.milestones.size} milestones)" }
                        }
                        _questionnaireStep.value = QuestionnaireStep.RESULTS
                    }
                }
                .onError { error ->
                    Logger.e("GoalViewModel") { "AI Goal Generation: Error - $error" }
                    _error.value = "Could not generate goals. Check your internet connection and try again."
                    _questionnaireStep.value = QuestionnaireStep.INPUT
                }
        } catch (e: Exception) {
            Logger.e("GoalViewModel", e) { "AI Goal Generation: Exception - ${e.message}" }
            _error.value = "Something went wrong. Please try again."
            _questionnaireStep.value = QuestionnaireStep.INPUT
        } finally {
            _isGeneratingPersonalizedGoals.value = false
        }
    }
}

/**
 * Add a specific generated goal to the main goals list.
 */
fun GoalViewModel.addGeneratedGoalToList(goal: Goal) {
    viewModelScope.launch {
        try {
            createGoalUseCase(goal)
            Analytics.goalCreated(goal.category.name, "ai_generated", hasAiGenerated = true)
            smartReminderManager.syncRemindersForGoal(goal)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to add goal: ${e.message}"
        }
    }
}

/**
 * Add all generated goals to the main goals list.
 */
fun GoalViewModel.addAllGeneratedGoalsToList() {
    viewModelScope.launch {
        try {
            val goals = _generatedGoalsFromAI.value
            goals.forEach { goal ->
                createGoalUseCase(goal)
                smartReminderManager.syncRemindersForGoal(goal)
            }
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to add goals: ${e.message}"
        }
    }
}

/**
 * Check if questionnaire is complete.
 */
fun GoalViewModel.isQuestionnaireComplete(): Boolean {
    val totalQuestions = _questions.value.sumOf { it.questions.size }
    val answeredQuestions = _userAnswers.value.answers.size
    return totalQuestions > 0 && answeredQuestions >= totalQuestions
}

/**
 * Reset questionnaire flow.
 */
fun GoalViewModel.resetQuestionnaire() {
    _userPrompt.value = ""
    _questions.value = emptyList()
    _userAnswers.value = UserQuestionnaireAnswers(emptyList())
    _questionnaireStep.value = QuestionnaireStep.INPUT
    _error.value = null
}
