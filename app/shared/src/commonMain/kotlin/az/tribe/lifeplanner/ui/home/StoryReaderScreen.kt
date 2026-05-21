package az.tribe.lifeplanner.ui.home

import androidx.compose.runtime.Composable
import az.tribe.lifeplanner.ui.components.StoryFullReader
import az.tribe.lifeplanner.ui.components.StoryReaderStore

@Composable
fun StoryReaderScreen(
    onNavigateBack: () -> Unit
) {
    val store = StoryReaderStore
    if (store.stories.isEmpty()) {
        onNavigateBack()
        return
    }

    StoryFullReader(
        stories = store.stories,
        initialIndex = store.initialIndex,
        seenIds = store.seenIds,
        onMarkSeen = store.onMarkSeen,
        onStoryAction = { action ->
            onNavigateBack()
            store.onStoryAction(action)
        },
        onDismiss = onNavigateBack
    )
}
