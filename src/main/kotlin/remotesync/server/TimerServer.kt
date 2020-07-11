package remotesync.server


import GUI
import config.Config
import getTimeAsClock
import gui.Refreshable
import objects.OBSSceneTimer
import objects.OBSState
import objects.notifications.Notifications
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import remotesync.RemoteSyncRefreshableRegister
import remotesync.objects.TimerMessage
import java.util.logging.Logger

object TimerServer : Server(), Refreshable {
    private val logger = Logger.getLogger(TimerServer::class.java.name)

    private val port = Config.remoteSyncServerPort
    private var isSetup = false

    private fun setup() {
        logger.info("Setting up timer server")

        val connector = ServerConnector(this)
        connector.port = port
        addConnector(connector)

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = "/"
        handler = context

        val servletHolder = ServletHolder("ws-events", EventServlet::class.java)
        context.addServlet(servletHolder, "/")
        isSetup = true
    }

    /**
     * startServer() will start a Jetty server, which by default starts in its own thread
     */
    fun startServer() {
        if (!isSetup) {
            try {
                setup()
            } catch (e: Exception) {
                e.printStackTrace()
                Notifications.add("Failed to initialize server: ${e.localizedMessage}", "Remote Sync")
                RemoteSyncRefreshableRegister.remoteSyncServerRefreshConnectionState()
            }
        }

        logger.info("Starting timer server")
        try {
            start()
            GUI.register(this)
        } catch (e: Exception) {
            e.printStackTrace()
            Notifications.add("Could not start server: ${e.localizedMessage}", "Remote Sync")
            RemoteSyncRefreshableRegister.remoteSyncServerRefreshConnectionState()
            return
        }

        logger.info("Timer server started")
        RemoteSyncRefreshableRegister.remoteSyncServerRefreshConnectionState()
    }

    fun stopServer() {
        logger.info("Stopping timer server")

        GUI.unregister(this)
        try {
            stop()
        } catch (e: Exception) {
            e.printStackTrace()
            Notifications.add("Could not stop server: ${e.localizedMessage}", "Remote Sync")
        }

        logger.info("Timer server stopped")
        Notifications.add("Remote sync server stopped", "Remote Sync")
        RemoteSyncRefreshableRegister.remoteSyncServerRefreshConnectionState()
    }

    override fun switchedScenes() {
        if (Config.remoteSyncClientEnabled) {
            return
        }

        Thread { updateClientsWithTimerMessage() }.run()
    }

    override fun refreshTimer() {
        if (Config.remoteSyncClientEnabled) {
            return
        }

        Thread { updateClientsWithTimerMessage() }.run()
    }

    private fun updateClientsWithTimerMessage() {
        val timerMessageJson = getCurrentTimerMessage().json()
        ServerStatus.clients.values.forEach {
            it.remote.sendString(timerMessageJson)
        }
    }

    fun getCurrentTimerMessage(): TimerMessage {
        return TimerMessage(
            sceneName = OBSState.currentScene.name,
            elapsedTime = OBSSceneTimer.getTimerAsClock(),
            elapsedTimeRaw = OBSSceneTimer.getValue(),
            timerState = OBSSceneTimer.getTimerState(),
            isTimed = (OBSSceneTimer.getMaxTimerValue() > 0),
            remainingTime = getTimeAsClock(OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getValue()),
            remainingTimeRaw = OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getValue(),
            maximumTime = OBSSceneTimer.getMaxTimerValue()
        )
    }
}