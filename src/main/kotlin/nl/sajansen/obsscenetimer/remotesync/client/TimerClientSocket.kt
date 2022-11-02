package nl.sajansen.obsscenetimer.remotesync.client

import com.google.gson.Gson
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.remotesync.objects.TimerMessage
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

@WebSocket
class TimerClientSocket(
    private val onConnectCallback: () -> Unit,
    private val onCloseCallback: (reason: String?) -> Unit
) {

    private val logger = LoggerFactory.getLogger(TimerClientSocket::class.java.name)

    private var session: Session? = null
    private val latch = CountDownLatch(1)

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        logger.info("Connected to server: ${session.remoteAddress.hostString}")
        this.session = session
        latch.countDown()

        onConnectCallback.invoke()
    }

    @OnWebSocketMessage
    fun onTextMessage(message: String) {
        logger.debug("Received message: $message")

        val timerMessage = try {
            Gson().fromJson(message, TimerMessage::class.java)
        } catch (e: Exception) {
            logger.warn("Failed to convert received message to json: $message")
            e.printStackTrace()
            return
        }

        processTimerMessage(timerMessage)
    }

    @OnWebSocketError
    fun onSocketError(t: Throwable) {
        logger.error("Connection error received. ${t.localizedMessage}")
        t.printStackTrace()
    }

    @OnWebSocketClose
    fun onClose(session: Session, @Suppress("UNUSED_PARAMETER") status: Int, reason: String?) {
        logger.info("Connection closed with: ${session.remoteAddress.hostString}. Reason: $reason")
        Notifications.add("Connection with timer server lost", "Scene Timer")

        onCloseCallback.invoke(reason)
    }

    fun sendMessage(message: String) {
        logger.info("Sending message: $message")
        if (session == null) {
            logger.warn("Cannot send message: not connected")
            return
        }

        try {
            session!!.remote.sendString(message)
        } catch (e: Exception) {
            logger.error("Failed to send message to timer server. ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    fun disconnect() {
        logger.info("Disconnecting client socket")
        session?.close()
    }

    private fun processTimerMessage(message: TimerMessage) {
        OBSSceneTimer.timerMessage = message
        OBSState.currentScene = TScene(message.sceneName)
        GUI.switchedScenes()
        GUI.refreshTimer()
    }
}