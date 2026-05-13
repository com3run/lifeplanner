package az.tribe.lifeplanner.ui.components

import java.io.File

actual fun getVideoCacheDir(): String {
    val dir = File(System.getProperty("java.io.tmpdir"), "lifeplanner_videos")
    dir.mkdirs()
    return dir.absolutePath
}

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun writeBytes(path: String, bytes: ByteArray) = File(path).writeBytes(bytes)
