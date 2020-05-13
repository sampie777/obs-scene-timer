import config.Config
import gui.MainFrame
import objects.OBSClient
import objects.notifications.Notifications
import themes.Theme
import java.awt.EventQueue
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.Exception

fun main(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    logger.info("Starting application")
    LogService.logBuffer.add(LogRecord(Level.INFO, "Starting application"))
    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)
    LogService.logBuffer.add(LogRecord(Level.INFO, "Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath))

    try {
        LogService.setup(args)
    } catch (e: Exception) {
        logger.severe("Failed to initiate logging: $e")
        e.printStackTrace()
        Notifications.add("Failed to setup logging service", "Application")
    }

    Config.enableWriteToFile(true)
    Config.load()
    Config.save()

    Theme.init()

    EventQueue.invokeLater {
        MainFrame.createAndShow()
    }

    OBSClient.start()
}
