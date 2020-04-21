import config.Config
import gui.MainFrame
import objects.SceneLogger
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.util.logging.Logger
import javax.swing.ImageIcon
import kotlin.system.exitProcess

private val logger: Logger = Logger.getLogger("utils")

fun getTimeAsClock(value: Long): String {
    var positiveValue = value

    var signString = ""
    if (value < 0) {
        positiveValue *= -1
        signString = "-"
    }

    val timerHours = positiveValue / 3600
    val timerMinutes = (positiveValue - timerHours * 3600) / 60
    val timerSeconds = positiveValue - timerHours * 3600 - timerMinutes * 60
    return String.format("%s%d:%02d:%02d", signString, timerHours, timerMinutes, timerSeconds)
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

fun createImageIcon(path: String): ImageIcon? {
    val imgURL: URL? = MainFrame::class.java.getResource(path)
    if (imgURL != null) {
        return ImageIcon(imgURL)
    }

    logger.severe("Couldn't find imageIcon: $path")
    return null
}

fun exitApplication() {
    logger.info("Shutting down application...")
    try {
        Config.save()
        SceneLogger.log("")
    } catch (t: Throwable) {
        logger.warning("Failed to properly shut down the application")
        t.printStackTrace()
    }
    exitProcess(0)
}