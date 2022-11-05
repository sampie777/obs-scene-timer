import com.google.gson.GsonBuilder
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.mainFrame.MainFrame
import nl.sajansen.obsscenetimer.objects.SceneLogger
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSClient
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("utils.common")

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
        logger.info("Stopping OBS client...")
        OBSClient.stop(true)
    } catch (t: Throwable) {
        logger.warn("Failed to correctly stop OBS client")
        t.printStackTrace()
    }

    try {
        logger.info("Closing windows...")
        GUI.windowClosing(MainFrame.getInstance())
    } catch (t: Throwable) {
        logger.warn("Failed to correctly close windows")
        t.printStackTrace()
    }

    try {
        logger.info("Shutting down scene logger...")
        SceneLogger.log("")
    } catch (t: Throwable) {
        logger.warn("Failed to shutdown scene logger")
        t.printStackTrace()
    }

    try {
        logger.info("Saving configuration...")
        Config.save()
    } catch (t: Throwable) {
        logger.warn("Failed to save configuration")
        t.printStackTrace()
    }

    try {
        logger.info("Closing rollbar...")
        Rollbar.close(true)
    } catch (t: Throwable) {
        if (Rollbar.isEnabled()) {
            logger.warn("Failed to close rollbar")
            t.printStackTrace()
        } else {
            logger.info("Failed to close rollbar but it's not enabled")
        }
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

fun Date.format(format: String): String? = SimpleDateFormat(format).format(this)

internal fun jsonBuilder() =
    GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .serializeNulls()
        .create()

fun openWebURL(url: String, subject: String = "Webbrowser"): Boolean {
    if (!Desktop.isDesktopSupported()) {
        logger.warn("Cannot open link '$url': not supported by host")
        return false
    }
    try {
        Desktop.getDesktop().browse(URI(url))
        return true
    } catch (t: Throwable) {
        logger.error("Error during opening link '$url'. ${t.localizedMessage}")
        Rollbar.error(t, mapOf("url" to url), "Error during opening link '$url'")
        t.printStackTrace()
        Notifications.popup("Failed to open link: $url", subject)
    }
    return false
}

fun <R> (() -> R).invokeWithCatch(
    logger: Logger? = null,
    logMessage: ((t: Throwable) -> String)? = null,
    notificationMessage: ((t: Throwable) -> String)? = null,
    notificationTitle: String = "OBS",
    rollbarCustomObjects: Map<String, Any>? = null,
    defaultReturnValue: R? = null,
): R? {
    return try {
        this.invoke()
    } catch (t: Throwable) {
        if (logger != null && logMessage != null) {
            val message = logMessage.invoke(t)
            logger.error(message)
            Rollbar.error(t, rollbarCustomObjects, message)
        }

        t.printStackTrace()

        if (notificationMessage != null) {
            Notifications.add(notificationMessage.invoke(t), notificationTitle)
        }
        defaultReturnValue
    }
}

fun <R> runWithCatch(
    function: () -> R,
    logger: Logger? = null,
    logMessage: ((t: Throwable) -> String)? = null,
    notificationMessage: ((t: Throwable) -> String)? = null,
    notificationTitle: String = "OBS",
    rollbarCustomObjects: Map<String, Any>? = null,
    defaultReturnValue: R? = null,
) = function.invokeWithCatch(
    logger = logger,
    logMessage = logMessage,
    notificationMessage = notificationMessage,
    notificationTitle = notificationTitle,
    rollbarCustomObjects = rollbarCustomObjects,
    defaultReturnValue = defaultReturnValue,
)