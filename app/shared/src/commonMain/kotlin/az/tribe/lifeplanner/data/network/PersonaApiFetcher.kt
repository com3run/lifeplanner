package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.BuildKonfig
import az.tribe.lifeplanner.data.repository.BuiltinCoachStore
import az.tribe.lifeplanner.database.CachedPersonaEntity
import az.tribe.lifeplanner.database.LifePlannerDBQueries
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachAvatar
import az.tribe.lifeplanner.domain.model.CoachMedia
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CoachProfile
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import kotlin.time.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// ── Defaults & string constants ──────────────────────────────────────────────

private object PersonaDefaults {
    const val TITLE = "Coach"
    const val CATEGORY = "WELLBEING"
    const val EMOJI = "✨"
    const val AVATAR_BG = "#6366F1"
    const val AVATAR_ACCENT = "#818CF8"
    const val AVATAR_ICON = "star"
    const val TIMEZONE = "UTC"
    fun greeting(name: String): String = "Hi! I'm $name. How can I help you today?"
}

private object MediaKind {
    const val VIDEO = "video"
    const val IMAGE = "image"
}

private object MediaFormat {
    const val REELS = "reels"
}

// ── Asset URL helpers ────────────────────────────────────────────────────────

private const val ASSET_BASE =
    "https://jimiwruckhlxkmnduhrn.supabase.co/storage/v1/object/public/assets/"

private fun avatarUrl(slug: String) = "${ASSET_BASE}coaches/$slug.png"
private fun posterUrl(slug: String) = "${ASSET_BASE}coaches/$slug-poster.jpg"
private fun clipUrl(slug: String) = "${ASSET_BASE}coaches/$slug-clip.mp4"

private fun resolveAvatar(avatarUrl: String?, imageUrl: String?, slug: String): String =
    avatarUrl ?: imageUrl ?: avatarUrl(slug)

private fun parseCategory(name: String?): GoalCategory =
    GoalCategory.entries.firstOrNull { it.name == name } ?: GoalCategory.WELLBEING

private fun nowIso(): String = Clock.System.now().toString()

private val specialtiesSerializer = ListSerializer(String.serializer())
private fun encodeSpecialties(values: List<String>?): String? =
    values?.let { Json.encodeToString(specialtiesSerializer, it) }
private fun decodeSpecialties(raw: String?): List<String> =
    raw?.let {
        runCatching { Json.decodeFromString(specialtiesSerializer, it) }.getOrDefault(emptyList())
    } ?: emptyList()

// ── TribeBot raw DTO (persona-api) ──────────────────────────────────────────

@Serializable
private data class TribeBotMediaDto(
    val id: String = "",
    val kind: String = "",       // MediaKind.VIDEO | MediaKind.IMAGE
    val format: String = "",     // MediaFormat.REELS | "image"
    val url: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val topic: String? = null,
    val caption: String? = null,
)

@Serializable
private data class TribeBotPersonaDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val style: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("clip_url") val clipUrl: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_public") val isPublic: Boolean = true,
    val media: List<TribeBotMediaDto> = emptyList(),
)

@Serializable
private data class TribeBotPersonaListResponse(
    val personas: List<TribeBotPersonaDto>? = null,
    val persona: TribeBotPersonaDto? = null,
    val count: Int? = null,
)

// ── LifePlanner personas_cache DTO (rich, from our own Supabase) ─────────────

@Serializable
private data class PersonasCacheDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val title: String = PersonaDefaults.TITLE,
    val description: String? = null,
    val style: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val category: String = PersonaDefaults.CATEGORY,
    val emoji: String = PersonaDefaults.EMOJI,
    val greeting: String? = null,
    val specialties: List<String>? = null,
    val personality: String? = null,
    @SerialName("avatar_bg_color") val avatarBgColor: String = PersonaDefaults.AVATAR_BG,
    @SerialName("avatar_accent_color") val avatarAccentColor: String = PersonaDefaults.AVATAR_ACCENT,
    @SerialName("avatar_icon_name") val avatarIconName: String = PersonaDefaults.AVATAR_ICON,
    val bio: String? = null,
    @SerialName("fun_fact") val funFact: String? = null,
    @SerialName("xp_to_unlock") val xpToUnlock: Int = 0,
    @SerialName("is_default_unlocked") val isDefaultUnlocked: Boolean = true,
    val timezone: String? = null,
    val city: String? = null,
    @SerialName("country_flag") val countryFlag: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

// ── Normalized intermediate so all sources funnel through one mapper ────────

private data class CoachFields(
    val id: String,
    val name: String,
    val title: String,
    val categoryName: String,
    val emoji: String,
    val greeting: String?,
    val specialties: List<String>,
    val personality: String?,
    val description: String?,
    val avatarBgColor: String,
    val avatarAccentColor: String,
    val avatarIconName: String,
    val bio: String?,
    val funFact: String?,
    val xpToUnlock: Int,
    val isDefaultUnlocked: Boolean,
    val imageUrl: String?,
    val avatarUrl: String?,
    val slug: String?,
    val timezone: String,
    val city: String,
    val countryFlag: String,
    val clipUrl: String?,
    val posterUrl: String?,
    val media: List<CoachMedia> = emptyList(),
) {
    fun toCoachPersona(): CoachPersona {
        val effectiveSlug = slug ?: id
        return CoachPersona(
            id = id,
            name = name,
            title = title,
            category = parseCategory(categoryName),
            emoji = emoji,
            greeting = greeting ?: PersonaDefaults.greeting(name),
            specialties = specialties,
            personality = personality ?: description ?: "",
            avatar = CoachAvatar(
                backgroundColor = avatarBgColor,
                accentColor = avatarAccentColor,
                iconName = avatarIconName,
            ),
            profile = CoachProfile(
                bio = bio ?: description ?: "",
                funFact = funFact ?: "",
                xpToUnlock = xpToUnlock,
                isDefaultUnlocked = isDefaultUnlocked,
            ),
            imageUrl = resolveAvatar(avatarUrl, imageUrl, effectiveSlug),
            posterUrl = posterUrl,
            clipUrl = clipUrl,
            timezone = timezone,
            city = city,
            countryFlag = countryFlag,
            media = media,
        )
    }
}

private fun PersonasCacheDto.toFields(): CoachFields {
    val effectiveSlug = slug ?: id
    return CoachFields(
        id = id, name = name, title = title, categoryName = category, emoji = emoji,
        greeting = greeting, specialties = specialties ?: emptyList(),
        personality = personality, description = description,
        avatarBgColor = avatarBgColor, avatarAccentColor = avatarAccentColor, avatarIconName = avatarIconName,
        bio = bio, funFact = funFact, xpToUnlock = xpToUnlock, isDefaultUnlocked = isDefaultUnlocked,
        imageUrl = imageUrl, avatarUrl = avatarUrl, slug = slug,
        timezone = timezone ?: PersonaDefaults.TIMEZONE,
        city = city ?: "", countryFlag = countryFlag ?: "",
        clipUrl = clipUrl(effectiveSlug),
        posterUrl = posterUrl(effectiveSlug),
    )
}

private fun CachedPersonaEntity.toFields(): CoachFields = CoachFields(
    id = id, name = name, title = title, categoryName = category, emoji = emoji,
    greeting = greeting, specialties = decodeSpecialties(specialties),
    personality = personality, description = description,
    avatarBgColor = avatar_bg_color, avatarAccentColor = avatar_accent_color, avatarIconName = avatar_icon_name,
    bio = bio, funFact = fun_fact, xpToUnlock = xp_to_unlock.toInt(),
    isDefaultUnlocked = is_default_unlocked == 1L,
    imageUrl = image_url, avatarUrl = avatar_url, slug = slug,
    timezone = timezone ?: PersonaDefaults.TIMEZONE,
    city = city ?: "", countryFlag = country_flag ?: "",
    // No clip_url column in SQLite cache — leave null so mergeMedia keeps the
    // builtin coach's real clipUrl unchanged.
    clipUrl = null,
    posterUrl = null,
)

private fun TribeBotMediaDto.toCoachMedia() = CoachMedia(
    id = id, kind = kind, url = url, thumbnailUrl = thumbnailUrl,
    topic = topic ?: "", caption = caption ?: "",
)

private fun TribeBotPersonaDto.toFields(): CoachFields {
    val effectiveSlug = slug ?: id
    val clipMedia = media.firstOrNull { it.kind == MediaKind.VIDEO && it.format == MediaFormat.REELS }
    // Top-level clip_url / video_url take priority; fallback to first reel in media[].
    val resolvedClipUrl = clipUrl ?: videoUrl ?: clipMedia?.url
    val resolvedPosterUrl = clipMedia?.thumbnailUrl ?: posterUrl ?: posterUrl(effectiveSlug)
    // Skip format="reels" images — they are slide thumbnails for video reels, not standalone content.
    val coachMedia = media
        .filter { !(it.kind == MediaKind.IMAGE && it.format == MediaFormat.REELS) }
        .map { it.toCoachMedia() }
    return CoachFields(
        id = id, name = name,
        title = PersonaDefaults.TITLE,
        categoryName = PersonaDefaults.CATEGORY,
        emoji = PersonaDefaults.EMOJI,
        greeting = null,
        specialties = emptyList(),
        personality = description, description = description,
        avatarBgColor = PersonaDefaults.AVATAR_BG,
        avatarAccentColor = PersonaDefaults.AVATAR_ACCENT,
        avatarIconName = PersonaDefaults.AVATAR_ICON,
        bio = description, funFact = null,
        xpToUnlock = 0, isDefaultUnlocked = true,
        imageUrl = imageUrl, avatarUrl = avatarUrl, slug = slug,
        timezone = PersonaDefaults.TIMEZONE,
        city = "", countryFlag = "",
        clipUrl = resolvedClipUrl, posterUrl = resolvedPosterUrl,
        media = coachMedia,
    )
}

private fun TribeBotPersonaDto.toCacheDto(): PersonasCacheDto = PersonasCacheDto(
    id = id,
    name = name,
    slug = slug,
    description = description,
    style = style,
    imageUrl = imageUrl,
    avatarUrl = avatarUrl ?: imageUrl,
    personality = description,
    bio = description,
    isActive = isPublic || isActive,
)

private suspend fun LifePlannerDBQueries.upsertPersonaRow(dto: PersonasCacheDto, fetchedAt: String) {
    upsertCachedPersona(
        id = dto.id,
        name = dto.name,
        description = dto.description,
        style = dto.style,
        image_url = dto.imageUrl,
        is_active = if (dto.isActive) 1L else 0L,
        fetched_at = fetchedAt,
        title = dto.title,
        category = dto.category,
        emoji = dto.emoji,
        greeting = dto.greeting,
        specialties = encodeSpecialties(dto.specialties),
        personality = dto.personality,
        avatar_bg_color = dto.avatarBgColor,
        avatar_accent_color = dto.avatarAccentColor,
        avatar_icon_name = dto.avatarIconName,
        bio = dto.bio,
        fun_fact = dto.funFact,
        xp_to_unlock = dto.xpToUnlock.toLong(),
        is_default_unlocked = if (dto.isDefaultUnlocked) 1L else 0L,
        timezone = dto.timezone,
        city = dto.city,
        country_flag = dto.countryFlag,
        slug = dto.slug,
        avatar_url = dto.avatarUrl,
    )
}

// ────────────────────────────────────────────────────────────────────────────

class PersonaApiFetcher(
    private val httpClient: HttpClient,
    private val db: SharedDatabase,
) {
    companion object {
        private const val TRIBEBOT_API_URL =
            "https://jimiwruckhlxkmnduhrn.supabase.co/functions/v1/persona-api" +
            "?includeMedia=true&mediaLimit=24&sort=recent"
        private const val CACHE_TABLE = "personas_cache"
    }

    private val log = Logger.withTag("PersonaApiFetcher")

    /** Merge persisted TribeBot media from local SQLDelight into BuiltinCoachStore. No network needed. */
    suspend fun loadCache() {
        try {
            val cached = db { it.lifePlannerDBQueries.getAllCachedPersonas().executeAsList() }
            val coaches = cached.map { it.toFields().toCoachPersona() }
            if (coaches.isNotEmpty()) {
                BuiltinCoachStore.mergeMedia(coaches)
                log.i { "Merged media from ${coaches.size} cached personas" }
            }
        } catch (e: Exception) {
            log.w { "Failed to load cached personas: ${e.message}" }
        }
    }

    /**
     * Fetch personas from LifePlanner's own personas_cache table (populated by TribeBot webhook).
     * Falls back to calling TribeBot API directly if the cache is empty.
     */
    suspend fun fetch() {
        val fetched = fetchFromLifePlannerCache()
        if (fetched) return
        log.i { "personas_cache empty — falling back to TribeBot API" }
        fetchFromTribeBot()
    }

    /** Pull from LifePlanner's global personas_cache with full auth headers. */
    private suspend fun fetchFromLifePlannerCache(): Boolean {
        return try {
            val response = httpClient.get(
                "${BuildKonfig.SUPABASE_URL}/rest/v1/$CACHE_TABLE?is_active=eq.true&select=*"
            ) {
                header("apikey", BuildKonfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${BuildKonfig.SUPABASE_ANON_KEY}")
                header("x-api-key", BuildKonfig.PERSONA_API_SECRET)
            }
            if (!response.status.isSuccess()) return false
            val rows = response.body<List<PersonasCacheDto>>()
            if (rows.isEmpty()) return false

            persistRows(rows)
            val coaches = rows.map { it.toFields().toCoachPersona() }
            BuiltinCoachStore.update(coaches)
            log.i { "Fetched ${coaches.size} coaches from personas_cache" }
            true
        } catch (e: Exception) {
            log.w { "Failed to fetch from personas_cache: ${e.message}" }
            false
        }
    }

    /** Direct call to TribeBot API — used as fallback when our cache is empty. */
    private suspend fun fetchFromTribeBot() {
        try {
            val response = httpClient.get(TRIBEBOT_API_URL) {
                header("x-api-key", BuildKonfig.PERSONA_API_SECRET)
            }
            if (!response.status.isSuccess()) {
                log.w { "TribeBot persona-api returned ${response.status}" }
                return
            }
            val body: TribeBotPersonaListResponse = response.body()
            val dtos = body.personas ?: listOfNotNull(body.persona)
            val active = dtos.filter { it.isPublic || it.isActive }
            if (active.isEmpty()) return

            persistRows(active.map { it.toCacheDto() })
            val tribeBotCoaches = active.map { it.toFields().toCoachPersona() }
            BuiltinCoachStore.mergeMedia(tribeBotCoaches)
            log.i { "Merged media from ${tribeBotCoaches.size} TribeBot personas" }
        } catch (e: Exception) {
            log.w { "Failed to fetch from TribeBot: ${e.message}" }
        }
    }

    private suspend fun persistRows(rows: List<PersonasCacheDto>) {
        if (rows.isEmpty()) return
        try {
            val fetchedAt = nowIso()
            db { database ->
                database.lifePlannerDBQueries.transaction {
                    rows.forEach { database.lifePlannerDBQueries.upsertPersonaRow(it, fetchedAt) }
                }
            }
        } catch (e: Exception) {
            log.w { "Failed to persist personas to local DB: ${e.message}" }
        }
    }
}
