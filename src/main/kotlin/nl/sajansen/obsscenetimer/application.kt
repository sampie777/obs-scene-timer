package nl.sajansen.obsscenetimer

import exitApplication
import getCurrentJarDirectory
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.mainFrame.MainFrame
import nl.sajansen.obsscenetimer.obs.OBSClient
import nl.sajansen.obsscenetimer.remotesync.client.TimerClient
import nl.sajansen.obsscenetimer.remotesync.server.TimerServer
import nl.sajansen.obsscenetimer.themes.Theme
import nl.sajansen.obsscenetimer.updater.UpdateChecker
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.EventQueue

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")

    logger.info("Starting application ${ApplicationInfo.artifactId}:${ApplicationInfo.version}")
    logger.info("Executing JAR directory: " + getCurrentJarDirectory(Config).absolutePath)

    Config.enableWriteToFile(true)
    Config.load()
    Rollbar.enable(Config.enableAutomaticErrorReporting)
    setObsParametersFromObsAddress()
    Config.save()

    Theme.init()

    EventQueue.invokeAndWait {
        try {
            MainFrame.createAndShow()
        }catch (t: Throwable) {
            logger.error("Failed to initialize GUI. ${t.localizedMessage}")
            Rollbar.error(t, "Failed to initialize GUI")
            t.printStackTrace()
            exitApplication()
        }
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
