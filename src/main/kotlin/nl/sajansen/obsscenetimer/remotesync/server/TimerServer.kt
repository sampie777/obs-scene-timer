package nl.sajansen.obsscenetimer.remotesync.server


import getTimeAsClock
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import nl.sajansen.obsscenetimer.remotesync.objects.TimerMessage
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.slf4j.LoggerFactory

object TimerServer : Server(), Refreshable {
    private val logger = LoggerFactory.getLogger(TimerServer::class.java.name)

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
                logger.error("Failed to initialize timer server. ${e.localizedMessage}")
                Rollbar.error(e, mapOf("port" to port), "Failed to initialize timer server")
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
            logger.error("Failed to start timer server. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("port" to port), "Failed to start timer server")
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

        Thread { updateClientsWithTimerMessage() }.start()
    }

    override fun refreshTimer() {
        if (Config.remoteSyncClientEnabled) {
            return
        }

        Thread { updateClientsWithTimerMessage() }.start()
    }

    private fun updateClientsWithTimerMessage() {
        val timerMessageJson = getCurrentTimerMessage().json()
        ServerStatus.clients.values.forEach {
            try {
                it.remote.sendString(timerMessageJson)
            } catch (t: Throwable) {
                logger.error("Failed to send timer message to client. ${t.localizedMessage}")
                Rollbar.error(
                    t, mapOf("message" to timerMessageJson, "clientsCount" to ServerStatus.clients.size, "client" to it, "remote" to it.remote),
                    "Failed to send timer message to client. ${t.localizedMessage}"
                )
                t.printStackTrace()
            }
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