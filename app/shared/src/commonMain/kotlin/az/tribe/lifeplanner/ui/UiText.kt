package az.tribe.lifeplanner.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * A string that comes from, or could come from, a string resource. Use it for anything that can be
 * localized (error messages, labels). Values that are always dynamic, such as a user's name or a
 * formatted date, stay plain `String`s in state.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        val id: org.jetbrains.compose.resources.StringResource,
        val args: Array<Any> = emptyArray(),
    ) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resource = id, formatArgs = args)
    }

    /** The same text outside composition, for one-shot events such as a snackbar. */
    suspend fun resolve(): String = when (this) {
        is DynamicString -> value
        is StringResource -> getString(id, *args)
    }
}
