package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.PermutationKind
import az.tribe.lifeplanner.domain.model.Possibility
import az.tribe.lifeplanner.domain.service.LocalPossibilityFallback
import co.touchlab.kermit.Logger
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 6, the divergence step (TRI-44). Given a stuck [Goal], asks the ai-proxy to *expand* the
 * option set using the book's cognitive permutations, then returns parsed [Possibility] options.
 * The prompt is framed so the AI widens choices and never decides for the user.
 */
@OptIn(ExperimentalUuidApi::class)
class GeneratePossibilitiesUseCase(
    private val aiProxyService: AiProxyService,
    private val fallback: LocalPossibilityFallback,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend operator fun invoke(goal: Goal): List<Possibility> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ageDays = goal.createdAt.date.daysUntil(today).coerceAtLeast(0)
        val userPrompt = buildString {
            appendLine("I am stuck on this goal and want to see more options, not be told what to do.")
            appendLine("Goal: ${goal.title}")
            appendLine("Area of life: ${goal.category.displayName}")
            if (goal.description.isNotBlank()) appendLine("Detail: ${goal.description}")
            append("Progress: ${goal.progress ?: 0}% after $ageDays days.")
        }

        val response = runCatching { aiProxyService.generateText(prompt = userPrompt, systemPrompt = SYSTEM_PROMPT) }
            .getOrElse {
                Logger.w("GeneratePossibilities") { "AI call failed, using local fallback: ${it.message}" }
                return fallback(goal)
            }

        return parse(response).ifEmpty { fallback(goal) }
    }

    private fun parse(response: String): List<Possibility> = runCatching {
        val cleaned = response.replace("```json", "").replace("```", "").trim()
        val start = cleaned.indexOf('[')
        val end = cleaned.lastIndexOf(']')
        if (start < 0 || end <= start) return emptyList()
        json.decodeFromString<List<PossibilityDto>>(cleaned.substring(start, end + 1))
            .filter { it.text.isNotBlank() }
            .map {
                Possibility(
                    id = Uuid.random().toString(),
                    text = it.text.trim(),
                    permutation = PermutationKind.fromString(it.permutation),
                    rationale = it.rationale.trim(),
                )
            }
            .take(8)
    }.getOrElse {
        Logger.w("GeneratePossibilities") { "Parse failed: ${it.message}" }
        emptyList()
    }

    @Serializable
    private data class PossibilityDto(
        val text: String = "",
        val permutation: String = "RECOMBINE",
        val rationale: String = "",
    )

    private companion object {
        val SYSTEM_PROMPT = """
            You are a divergent-thinking partner. The user is stuck on a goal. Your only job is to WIDEN
            their options. Do not pick one, rank them, or tell them what to do.

            Generate 6 genuinely different possibilities using these cognitive permutations:
            - RECOMBINE: combine the user's existing habits, goals, or resources in a new way.
            - ANALOGY: borrow an approach from a completely different domain.
            - QUESTION_ASSUMPTION: challenge a limiting belief baked into how the goal is framed.
            - INVERT: flip the problem, aim for the opposite, or remove something instead of adding.
            - SHRINK: the smallest version that still counts as progress.

            Rules:
            - Each possibility must be concrete and doable today, not vague advice.
            - Spread them across different permutations; never repeat the same idea twice.
            - Stay warm and non-judgemental. Write like a person. Never use the long dash punctuation.
            - Return ONLY a JSON array, with no prose and no markdown fences. Each item is exactly:
              {"text": "the option", "permutation": "RECOMBINE|ANALOGY|QUESTION_ASSUMPTION|INVERT|SHRINK", "rationale": "one short line on why this could unstick it"}
        """.trimIndent()
    }
}
