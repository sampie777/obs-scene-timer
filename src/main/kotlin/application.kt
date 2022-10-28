import config.Config
import gui.mainFrame.MainFrame
import objects.ApplicationInfo
import objects.notifications.Notifications
import obs.OBSClient
import remotesync.client.TimerClient
import remotesync.server.TimerServer
import themes.Theme
import updater.UpdateChecker
import java.awt.EventQueue
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

fun main(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    logger.info("Starting application ${ApplicationInfo.artifactId}:${ApplicationInfo.version}")
    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)
    LogService.logBuffer.add(
        LogRecord(
            Level.INFO,
            "Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath
        )
    )

    Config.enableWriteToFile(true)
    Config.load()
    setObsParametersFromObsAddress()
    setupLogging(args)  // Setup logging as soon as possible, but because it depends on Config, just let Config load first
    Config.save()

    Theme.init()

    EventQueue.invokeLater {
        MainFrame.createAndShow()
    }

    if ("--clear-update-history" in args) {
        UpdateChecker().clearUpdateHistory()
    }
    UpdateChecker().checkForUpdates()

    if (Config.remoteSyncClientEnabled) {
        logger.info("Start up with remote sync client enabled")
        TimerClient.connect(Config.remoteSyncClientAddress)
    } else {
        if (Config.remoteSyncServerEnabled) {
            logger.info("Start up with remote sync server enabled")
            TimerServer.startServer()
        }

        if ("--offline" !in args) {
            OBSClient.start()
        }
    }
}

fun setObsParametersFromObsAddress() {
    if (Config.obsAddress == "ws://${Config.obsHost}:${Config.obsPort}" || Config.obsAddress.isEmpty()) return

    Config.obsHost = Config.obsAddress.substringAfter("://").substringBeforeLast(":")
    val obsPort = Config.obsAddress.substringAfterLast(":")
    if (obsPort == Config.obsHost) {
        Config.obsPort = 4455   // Default port
    } else {
        Config.obsPort = obsPort.toIntOrNull() ?: 4455
    }
}

private fun setupLogging(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    try {
        LogService.setup(args)
    } catch (e: Exception) {
        logger.severe("Failed to initiate logging: $e")
        e.printStackTrace()
        Notifications.add("Failed to setup logging service: ${e.localizedMessage}", "Application")
    }
}
