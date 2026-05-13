package az.tribe.lifeplanner.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CoachMedia(
    val id: String,
    val kind: String,           // "video" | "image"
    val url: String,
    val thumbnailUrl: String?,
    val topic: String,
    val caption: String,
)

val CoachMedia.isVideo get() = kind == "video"
