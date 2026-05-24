package az.tribe.lifeplanner.ui.chat

import az.tribe.lifeplanner.domain.model.CoachSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

fun ChatViewModel.startGoalQuestionnaire(suggestion: CoachSuggestion.CreateGoal) {
    val state = _uiState.value
    if (suggestion.id in state.questionnairedSuggestionIds) return

    val intentText = "${suggestion.title}: ${suggestion.description}"
    _uiState.value = state.copy(
        goalQuestionnaire = ChatGoalQuestionnaire(
            forSuggestionId = suggestion.id,
            intentText = intentText,
            isLoading = true
        )
    )
    viewModelScope.launch {
        val (questions, error) = generateGoalQuestions(intentText)
        _uiState.value = _uiState.value.copy(
            goalQuestionnaire = _uiState.value.goalQuestionnaire?.copy(
                questions = questions,
                answers = List(questions.size) { emptyList<String>() },
                isLoading = false,
                loadError = error
            )
        )
    }
}

fun ChatViewModel.answerGoalQuestion(index: Int, answer: String) {
    val q = _uiState.value.goalQuestionnaire ?: return
    val newAnswers = q.answers.toMutableList()
    if (index < newAnswers.size) {
        val current = newAnswers[index]
        newAnswers[index] = if (answer in current) current - answer else current + answer
    }
    _uiState.value = _uiState.value.copy(
        goalQuestionnaire = q.copy(answers = newAnswers)
    )
}

fun ChatViewModel.submitGoalQuestionnaire() {
    val q = _uiState.value.goalQuestionnaire ?: return
    if (q.submitted) return

    val answersText = q.questions.zip(q.answers).joinToString("\n") { (question, selected) ->
        val answerStr = if (selected.isEmpty()) "-" else selected.joinToString(", ")
        "• ${question.text}: $answerStr"
    }
    val message = "Goal clarification answers for \"${q.intentText.take(60)}\":\n$answersText\n\nPlease create a personalised goal for me based on these answers."

    _uiState.value = _uiState.value.copy(
        goalQuestionnaire = q.copy(submitted = true),
        questionnairedSuggestionIds = _uiState.value.questionnairedSuggestionIds + q.forSuggestionId
    )
    sendMessage(message)
}

// Returns (questions, hadError). Always AI-generated, no static fallback.
internal suspend fun ChatViewModel.generateGoalQuestions(intentText: String): Pair<List<ChatGoalQuestion>, Boolean> {
    return try {
        val prompt = """
            The user wants to achieve: "$intentText"

            Generate exactly 7 clarifying questions to deeply personalise their goal.
            Every question must be specific to their stated intent, not generic filler.
            Each question must allow multiple answers and have 5-6 options tailored to their goal.
            For each question include at least one "tricky" or unexpected option that reveals hidden priorities.
            Always include "None of the above" as the final option for every question.
            Cover: motivation/why, prior experience, current obstacles, timeline, support system,
            definition of success, and commitment level.
        """.trimIndent()

        val schema = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("questions") {
                    put("type", "array")
                    putJsonObject("items") {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("question") { put("type", "string") }
                            putJsonObject("options") {
                                put("type", "array")
                                putJsonObject("items") { put("type", "string") }
                            }
                        }
                        putJsonArray("required") {
                            add(JsonPrimitive("question"))
                            add(JsonPrimitive("options"))
                        }
                    }
                }
            }
            putJsonArray("required") { add(JsonPrimitive("questions")) }
        }

        val responseText = withContext(Dispatchers.IO) {
            aiProxy.generateStructuredJson(prompt, schema)
        }

        val json = Json { ignoreUnknownKeys = true }
        val obj = json.parseToJsonElement(responseText).jsonObject
        val parsed = obj["questions"]?.jsonArray?.mapNotNull { el ->
            val qObj = el.jsonObject
            val q = qObj["question"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val opts = qObj["options"]?.jsonArray
                ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?: return@mapNotNull null
            if (opts.isEmpty()) null else ChatGoalQuestion(q, opts)
        } ?: emptyList()

        Pair(parsed, parsed.isEmpty())
    } catch (_: Exception) {
        Pair(emptyList(), true)
    }
}
