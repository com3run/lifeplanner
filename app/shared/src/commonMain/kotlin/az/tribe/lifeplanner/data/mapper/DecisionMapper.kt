package az.tribe.lifeplanner.data.mapper

import az.tribe.lifeplanner.database.DecisionEntity
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.DecisionSource
import az.tribe.lifeplanner.domain.model.DecisionStatus
import az.tribe.lifeplanner.domain.model.OutcomeQuality
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

private const val OPTIONS_DELIM = "\n"

fun DecisionEntity.toDomain(): Decision = Decision(
    id = id,
    question = question,
    optionsConsidered = if (optionsConsidered.isBlank()) emptyList() else optionsConsidered.split(OPTIONS_DELIM),
    chosenOption = chosenOption,
    reasoning = reasoning,
    relatedGoalId = relatedGoalId,
    expectedOutcome = expectedOutcome,
    confidence = confidence.toInt(),
    decidedAt = LocalDateTime.parse(decidedAt),
    actualOutcome = actualOutcome,
    outcomeReviewedAt = outcomeReviewedAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
    outcomeQuality = outcomeQuality?.let { runCatching { OutcomeQuality.valueOf(it) }.getOrNull() },
    // Unknown/legacy values fall back to the born-confirmed choice-point default.
    source = runCatching { DecisionSource.valueOf(source) }.getOrDefault(DecisionSource.CHOICE_POINT),
    status = runCatching { DecisionStatus.valueOf(status) }.getOrDefault(DecisionStatus.CONFIRMED),
)

fun Decision.toEntity(): DecisionEntity = DecisionEntity(
    id = id,
    question = question,
    optionsConsidered = optionsConsidered.joinToString(OPTIONS_DELIM),
    chosenOption = chosenOption,
    reasoning = reasoning,
    relatedGoalId = relatedGoalId,
    expectedOutcome = expectedOutcome,
    confidence = confidence.toLong(),
    decidedAt = decidedAt.toString(),
    actualOutcome = actualOutcome,
    outcomeReviewedAt = outcomeReviewedAt?.toString(),
    outcomeQuality = outcomeQuality?.name,
    source = source.name,
    status = status.name,
    sync_updated_at = Clock.System.now().toString(),
    is_deleted = 0L,
    sync_version = 0L,
    last_synced_at = null
)

fun List<DecisionEntity>.toDomainDecisions(): List<Decision> = map { it.toDomain() }
