package remotesync.client

import com.google.gson.Gson
import objects.OBSSceneTimer
import objects.OBSState
import objects.notifications.Notifications
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import remotesync.objects.TimerMessage
import java.util.concurrent.CountDownLatch
import java.util.logging.Logger

@WebSocket
class TimerClientSocket(
    private val onConnectCallback: () -> Unit,
    private val onCloseCallback: (reason: String?) -> Unit
) {

    private val logger = Logger.getLogger(TimerClientSocket::class.java.name)

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
        logger.fine("Received message: $message")

        val timerMessage = try {
            Gson().fromJson(message, TimerMessage::class.java)
        } catch (e: Exception) {
            logger.warning("Failed to convert received message to json: $message")
            e.printStackTrace()
            return
        }

        processTimerMessage(timerMessage)
    }

    @OnWebSocketError
    fun onSocketError(t: Throwable) {
        logger.severe("Connection error received")
        t.printStackTrace()
    }

    @OnWebSocketClose
    fun onClose(session: Session, reason: String?) {
        logger.info("Connection closed with: ${session.remoteAddress.hostString}. Reason: $reason")
        Notifications.add("Connection with timer server lost", "Scene Timer")

        onCloseCallback.invoke(reason)
    }

    fun sendMessage(message: String) {
        logger.info("Sending message: $message")
        if (session == null) {
            logger.warning("Cannot send message: not connected")
            return
        }

        try {
            session!!.remote.sendString(message)
        } catch (e: Exception) {
            logger.severe("Failed to send message to timer server")
            e.printStackTrace()
        }
    }

    fun disconnect() {
        logger.info("Disconnecting client socket")
        session?.close()
    }

    private fun processTimerMessage(message: TimerMessage) {
        OBSSceneTimer.timerMessage = message
        OBSState.currentSceneName = message.sceneName
        GUI.switchedScenes()
        GUI.refreshTimer()
    }
}