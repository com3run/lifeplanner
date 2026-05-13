package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.model.BodySlice
import az.tribe.lifeplanner.domain.model.CareerSlice
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.MetaSlice
import az.tribe.lifeplanner.domain.model.MoneySlice
import az.tribe.lifeplanner.domain.model.PeopleSlice
import az.tribe.lifeplanner.domain.model.PurposeSlice
import az.tribe.lifeplanner.domain.model.UserSituation
import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class MemoryUpdateTag(
    val sliceName: String,
    val jsonData: String,
    val rawTag: String
)

class CoachOrchestrator {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ===== Situation context for prompt injection =====

    fun buildSituationContext(situation: UserSituation, coach: CoachPersona?): String {
        val lines = mutableListOf<String>()

        // Meta slice — always included if known
        val meta = situation.meta
        if (meta.confidence > 0f) {
            buildList {
                meta.name?.let { add("Name: $it") }
                meta.age?.let { add("Age: $it") }
                meta.lifeStage?.let { add("Stage: ${it.name.lowercase().replace('_', ' ')}") }
                meta.stressLevel?.let { add("Stress: $it/10") }
                meta.sleepQuality?.let { add("Sleep quality: $it/10") }
                meta.overallMood?.let { add("Mood: $it/10") }
            }.let { if (it.isNotEmpty()) lines.add("About: ${it.joinToString(", ")}") }
        }

        // Coach-specific slice — each coach sees their own slice + full meta
        when (coach?.id) {
            "alex_career" -> situation.career.toPromptLine()?.let { lines.add("Career: $it") }
            "morgan_finance" -> situation.money.toPromptLine()?.let { lines.add("Money: $it") }
            "kai_fitness" -> situation.body.toPromptLine()?.let { lines.add("Body: $it") }
            "sam_social" -> situation.people.toPromptLine()?.let { lines.add("People: $it") }
            "river_wellness" -> situation.purpose.toPromptLine()?.let { lines.add("Purpose: $it") }
            "jamie_family"   -> situation.people.toPromptLine()?.let { lines.add("People: $it") }
            else -> {
                // Luna or unknown — show highest-confidence slices
                listOfNotNull(
                    situation.career.takeIf { it.confidence > 0.4f }?.toPromptLine()?.let { "Career: $it" },
                    situation.body.takeIf { it.confidence > 0.4f }?.toPromptLine()?.let { "Body: $it" },
                    situation.money.takeIf { it.confidence > 0.4f }?.toPromptLine()?.let { "Money: $it" }
                ).forEach { lines.add(it) }
            }
        }

        return if (lines.isEmpty()) "" else buildString {
            appendLine("USER PROFILE (use to personalize — do NOT re-ask what you already know here):")
            lines.forEach { appendLine("• $it") }
        }.trimEnd()
    }

    // ===== Missing slot detection (per coach category) =====

    fun getMissingSlots(situation: UserSituation, coachId: String): List<String> {
        return when (coachId) {
            "alex_career" -> buildList {
                with(situation.career) {
                    if (status == null) add("employment status")
                    if (status != null && role == null) add("current role")
                    if (careerGoal == null) add("career ambition")
                }
            }
            "morgan_finance" -> buildList {
                with(situation.money) {
                    if (incomeBand == null) add("income range")
                    if (savingsHabit == null) add("savings habits")
                    if (financialGoal == null) add("financial goal")
                }
            }
            "kai_fitness" -> buildList {
                with(situation.body) {
                    if (activityLevel == null) add("activity level")
                    if (sleepHours == null) add("sleep hours per night")
                    if (energyRating == null) add("energy level")
                }
            }
            "sam_social" -> buildList {
                with(situation.people) {
                    if (socialEnergy == null) add("introvert/extrovert")
                    if (closeCircleSize == null) add("close social circle size")
                }
            }
            "river_wellness" -> buildList {
                with(situation.purpose) {
                    if (topValues.isEmpty()) add("core values")
                    if (longTermVision == null) add("long-term vision")
                }
            }
            "jamie_family" -> buildList {
                with(situation.people) {
                    if (familyContext == null) add("family situation")
                    if (relationshipStatus == null) add("relationship status")
                    if (closeCircleSize == null) add("close social circle size")
                }
            }
            else -> buildList {
                with(situation.meta) {
                    if (name == null) add("name")
                    if (age == null) add("age")
                }
            }
        }
    }

    // ===== Memory update tag parser =====

    // Parses [UPDATE_SITUATION:{"slice":"career","role":"Engineer","confidence":0.8}]
    fun parseMemoryUpdateTag(text: String): MemoryUpdateTag? {
        val pattern = Regex("""\[UPDATE_SITUATION:(\{[^\]]+\})\]""")
        val match = pattern.find(text) ?: return null
        return try {
            val rawJson = match.groupValues[1]
            val jsonObj: JsonObject = json.parseToJsonElement(rawJson).jsonObject
            val sliceName = jsonObj["slice"]?.jsonPrimitive?.content ?: return null
            val dataObj = JsonObject(jsonObj.filterKeys { it != "slice" })
            MemoryUpdateTag(sliceName, dataObj.toString(), match.value)
        } catch (e: Exception) {
            Logger.w("CoachOrchestrator") { "Failed to parse UPDATE_SITUATION tag: ${e.message}" }
            null
        }
    }

    // Decode and apply the memory update to the appropriate slice
    fun applyUpdate(
        tag: MemoryUpdateTag,
        existing: UserSituation
    ): UserSituation {
        return try {
            when (tag.sliceName) {
                "meta" -> {
                    val update = json.decodeFromString<MetaSlice>(tag.jsonData)
                    existing.copy(meta = existing.meta.merge(update))
                }
                "career" -> {
                    val update = json.decodeFromString<CareerSlice>(tag.jsonData)
                    existing.copy(career = existing.career.merge(update))
                }
                "money" -> {
                    val update = json.decodeFromString<MoneySlice>(tag.jsonData)
                    existing.copy(money = existing.money.merge(update))
                }
                "body" -> {
                    val update = json.decodeFromString<BodySlice>(tag.jsonData)
                    existing.copy(body = existing.body.merge(update))
                }
                "people" -> {
                    val update = json.decodeFromString<PeopleSlice>(tag.jsonData)
                    existing.copy(people = existing.people.merge(update))
                }
                "purpose" -> {
                    val update = json.decodeFromString<PurposeSlice>(tag.jsonData)
                    existing.copy(purpose = existing.purpose.merge(update))
                }
                else -> existing
            }
        } catch (e: Exception) {
            Logger.w("CoachOrchestrator") { "Failed to apply memory update for ${tag.sliceName}: ${e.message}" }
            existing
        }
    }

    // ===== Slice merge helpers (null fields keep existing value) =====

    private fun MetaSlice.merge(u: MetaSlice) = copy(
        name = u.name ?: name,
        age = u.age ?: age,
        lifeStage = u.lifeStage ?: lifeStage,
        topPriority = u.topPriority ?: topPriority,
        overallMood = u.overallMood ?: overallMood,
        stressLevel = u.stressLevel ?: stressLevel,
        sleepQuality = u.sleepQuality ?: sleepQuality,
        confidence = maxOf(confidence, u.confidence)
    )

    private fun CareerSlice.merge(u: CareerSlice) = copy(
        status = u.status ?: status,
        role = u.role ?: role,
        industry = u.industry ?: industry,
        yearsExperience = u.yearsExperience ?: yearsExperience,
        topSkills = u.topSkills.ifEmpty { topSkills },
        hasResume = if (u.confidence > 0f) u.hasResume else hasResume,
        resumeUrl = u.resumeUrl ?: resumeUrl,
        wantsResumeService = u.wantsResumeService ?: wantsResumeService,
        careerGoal = u.careerGoal ?: careerGoal,
        confidence = maxOf(confidence, u.confidence)
    )

    private fun MoneySlice.merge(u: MoneySlice) = copy(
        incomeBand = u.incomeBand ?: incomeBand,
        currency = u.currency ?: currency,
        savingsHabit = u.savingsHabit ?: savingsHabit,
        hasDebt = u.hasDebt ?: hasDebt,
        financialGoal = u.financialGoal ?: financialGoal,
        riskAppetite = u.riskAppetite ?: riskAppetite,
        confidence = maxOf(confidence, u.confidence)
    )

    private fun BodySlice.merge(u: BodySlice) = copy(
        activityLevel = u.activityLevel ?: activityLevel,
        sleepHours = u.sleepHours ?: sleepHours,
        dietPattern = u.dietPattern ?: dietPattern,
        energyRating = u.energyRating ?: energyRating,
        flags = u.flags.ifEmpty { flags },
        confidence = maxOf(confidence, u.confidence)
    )

    private fun PeopleSlice.merge(u: PeopleSlice) = copy(
        relationshipStatus = u.relationshipStatus ?: relationshipStatus,
        closeCircleSize = u.closeCircleSize ?: closeCircleSize,
        familyContext = u.familyContext ?: familyContext,
        socialEnergy = u.socialEnergy ?: socialEnergy,
        confidence = maxOf(confidence, u.confidence)
    )

    private fun PurposeSlice.merge(u: PurposeSlice) = copy(
        topValues = u.topValues.ifEmpty { topValues },
        mindfulnessPractice = u.mindfulnessPractice ?: mindfulnessPractice,
        meaningSources = u.meaningSources.ifEmpty { meaningSources },
        longTermVision = u.longTermVision ?: longTermVision,
        confidence = maxOf(confidence, u.confidence)
    )

    // ===== Slice → prompt line helpers =====

    private fun CareerSlice.toPromptLine(): String? {
        if (confidence == 0f) return null
        return buildList {
            status?.let { add(it.name.lowercase()) }
            role?.let { add(it) }
            industry?.let { add(it) }
            yearsExperience?.let { add("${it}y exp") }
            topSkills.take(3).let { if (it.isNotEmpty()) add("skills: ${it.joinToString(", ")}") }
            careerGoal?.let { add("goal: $it") }
        }.joinToString(", ").takeIf { it.isNotEmpty() }
    }

    private fun MoneySlice.toPromptLine(): String? {
        if (confidence == 0f) return null
        return buildList {
            incomeBand?.let { add(it.name.lowercase().replace('_', '-')) }
            savingsHabit?.let { add("savings: ${it.name.lowercase()}") }
            hasDebt?.let { if (it) add("has debt") }
            financialGoal?.let { add("goal: $it") }
        }.joinToString(", ").takeIf { it.isNotEmpty() }
    }

    private fun BodySlice.toPromptLine(): String? {
        if (confidence == 0f) return null
        return buildList {
            activityLevel?.let { add(it.name.lowercase()) }
            sleepHours?.let { add("${it}h sleep") }
            energyRating?.let { add("energy: $it/10") }
            if (flags.isNotEmpty()) add("flags: ${flags.joinToString { it.name.lowercase() }}")
        }.joinToString(", ").takeIf { it.isNotEmpty() }
    }

    private fun PeopleSlice.toPromptLine(): String? {
        if (confidence == 0f) return null
        return buildList {
            relationshipStatus?.let { add(it.name.lowercase().replace('_', ' ')) }
            socialEnergy?.let { add(it.name.lowercase()) }
            closeCircleSize?.let { add("circle: ${it.name.lowercase()}") }
            familyContext?.let { add(it) }
        }.joinToString(", ").takeIf { it.isNotEmpty() }
    }

    private fun PurposeSlice.toPromptLine(): String? {
        if (confidence == 0f) return null
        return buildList {
            topValues.take(3).let { if (it.isNotEmpty()) add("values: ${it.joinToString(", ")}") }
            mindfulnessPractice?.let { if (it) add("practices mindfulness") }
            longTermVision?.let { add("vision: $it") }
        }.joinToString(", ").takeIf { it.isNotEmpty() }
    }
}

internal const val SITUATION_UPDATE_INSTRUCTION = """
PROFILE UPDATE (optional): If the user reveals new personal information during this conversation (job, age, goals, habits, relationships, values), append ONE hidden tag at the very end of your response:
[UPDATE_SITUATION:{"slice":"<name>","<field>":"<value>","confidence":<0.0-1.0>}]
Valid slices: meta, career, money, body, people, purpose
Only include fields you are confident about. Do NOT update if the user hasn't shared anything new."""
