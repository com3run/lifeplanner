package az.tribe.lifeplanner.di

import androidx.compose.runtime.Composable

@Composable
actual fun rememberFilePicker(onResult: (FilePickerResult) -> Unit): FilePicker {
    return object : FilePicker {
        override fun launchFilePicker() {
            onResult(FilePickerResult.Cancelled)
        }
    }
}
