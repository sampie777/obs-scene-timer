package utils

import com.xuggle.xuggler.IContainer
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger("utils.files")

fun getReadableFileSize(file: File): String {
    return when {
        file.length() > 1024 * 1024 -> {
            val fileSize = file.length().toDouble() / (1024 * 1024)
            String.format("%.2f MB", fileSize)
        }
        file.length() > 1024 -> {
            val fileSize = file.length().toDouble() / 1024
            String.format("%.2f kB", fileSize)
        }
        else -> {
            String.format("%d bytes", file.length())
        }
    }
}

fun getFileNameWithoutExtension(file: File): String {
    return file.name.substring(0, file.name.lastIndexOf('.'))
}

fun getFileExtension(file: File): String {
    return file.name.substring(file.name.lastIndexOf('.') + 1)
}

fun getVideoLengthOrZeroForFile(filename: String): Int {
    return try {
        logger.info("Trying to get video length for: $filename")
        getVideoLength(filename).toInt()
    } catch (t: Throwable) {
        logger.severe("Failed to get video length for '$filename': ${t.message}")
        t.printStackTrace()
        0
    }
}

/**
 * Returns video length in seconds, or 0 if file not found
 *
 * @param filename
 * @return
 */
fun getVideoLength(filename: String): Long {
    val file = File(filename)
    if (!file.exists()) {
        logger.warning("File does not exists: $filename")
        return 0
    }

    logger.info("Getting duration of: $filename")

    val container = IContainer.make()
    container.open(filename, IContainer.Type.READ, null)
    val duration = TimeUnit.MICROSECONDS.toSeconds(container.duration)
    logger.fine("Duration is: $duration")

    return duration
}