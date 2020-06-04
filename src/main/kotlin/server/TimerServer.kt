package server


import config.Config
import getTimeAsClock
import getTimerState
import gui.Refreshable
import objects.OBSSceneTimer
import objects.OBSState
import objects.notifications.Notifications
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import server.objects.TimerMessage
import java.util.logging.Logger

class TimerServer : Server(), Refreshable {
    private val logger = Logger.getLogger(TimerServer::class.java.name)

    private val port = Config.timerWebsocketPort

    init {
        val connector = ServerConnector(this)
        connector.port = port
        addConnector(connector)

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.contextPath = "/"
        handler = context

        val servletHolder = ServletHolder("ws-events", EventServlet::class.java)
        context.addServlet(servletHolder, "/")
    }

    fun startServer() {
        try {
            start()
            GUI.register(this)
        } catch (e: Exception) {
            e.printStackTrace()
            Notifications.add("Could not start server: ${e.localizedMessage}", "Websocket server")
        }
    }

    override fun switchedScenes() {
        val timerMessageJson = getCurrentTimerMessage().json()
        ServerStatus.clients.values.forEach {
            it.remote.sendString(timerMessageJson)
        }
    }

    override fun refreshTimer() {
        val timerMessageJson = getCurrentTimerMessage().json()
        ServerStatus.clients.values.forEach {
            it.remote.sendString(timerMessageJson)
        }
    }

    private fun getCurrentTimerMessage(): TimerMessage {
        return TimerMessage(
            sceneName = OBSState.currentSceneName,
            elapsedTime = OBSSceneTimer.getTimerAsClock(),
            timerPhase = getTimerState(),
            isTimed = (OBSSceneTimer.getMaxTimerValue() > 0),
            remainingTime = getTimeAsClock(OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getValue())
            )
    }
}