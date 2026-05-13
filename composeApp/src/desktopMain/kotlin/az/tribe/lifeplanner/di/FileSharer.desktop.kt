package az.tribe.lifeplanner.di

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class DesktopFileSharer : FileSharer {
    override fun shareFile(content: String, fileName: String, mimeType: String) {
        copyToClipboard(content)
    }

    override fun copyToClipboard(content: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(content), null)
    }
}

actual fun createFileSharer(): FileSharer = DesktopFileSharer()
