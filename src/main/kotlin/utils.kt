import com.google.gson.GsonBuilder
import config.Config
import gui.mainFrame.MainFrame
import objects.SceneLogger
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.system.exitProcess

private val logger: Logger = Logger.getLogger("utils")

fun getTimeAsClock(value: Long, looseFormat: Boolean = false): String {
    var positiveValue = value

    var signString = ""
    if (value < 0) {
        positiveValue *= -1
        signString = "-"
    }

    val timerHours = positiveValue / 3600
    val timerMinutes = (positiveValue - timerHours * 3600) / 60
    val timerSeconds = positiveValue - timerHours * 3600 - timerMinutes * 60

    if (!looseFormat || timerHours != 0L) {
        return String.format("%s%d:%02d:%02d", signString, timerHours, timerMinutes, timerSeconds)
    }

    return String.format("%s%d:%02d", signString, timerMinutes, timerSeconds)
}

@Throws(UnsupportedEncodingException::class)
fun getCurrentJarDirectory(caller: Any): File {
    val url = caller::class.java.protectionDomain.codeSource.location
    val jarPath = URLDecoder.decode(url.file, "UTF-8")
    return File(jarPath).parentFile
}

fun isAddressLocalhost(address: String): Boolean {
    return address.contains("localhost") || address.contains("127.0.0.1")
}

fun exitApplication() {
    logger.info("Shutting down application")

    MainFrame.getInstance()?.saveWindowPosition()

    try {
        logger.info("Closing windows...")
        GUI.windowClosing(MainFrame.getInstance())
    } catch (t: Throwable) {
        logger.warning("Failed to correctly close windows")
        t.printStackTrace()
    }

    try {
        logger.info("Shutting down scene logger...")
        SceneLogger.log("")
    } catch (t: Throwable) {
        logger.warning("Failed to shutdown scene logger")
        t.printStackTrace()
    }

    try {
        logger.info("Saving configuration...")
        Config.save()
    } catch (t: Throwable) {
        logger.warning("Failed to save configuration")
        t.printStackTrace()
    }

    logger.info("Shutdown finished")
    exitProcess(0)
}

fun brightness(color: Color): Double {
    return 0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue
}

fun decodeURI(uri: String): String {
    return URLDecoder.decode(uri, StandardCharsets.UTF_8.name())
}

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

fun Date.format(format: String): String? = SimpleDateFormat(format).format(this)

internal fun jsonBuilder() =
    GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .serializeNulls()
        .create()

fun openWebURL(url: String): Boolean {
    if (!Desktop.isDesktopSupported()) {
        logger.warning("Cannot open link '$url': not supported by host")
        return false
    }
    try {
        Desktop.getDesktop().browse(URI(url))
        return true
    } catch (e: IOException) {
        logger.severe("Error during opening link '$url'")
        e.printStackTrace()
    }
    return false
}