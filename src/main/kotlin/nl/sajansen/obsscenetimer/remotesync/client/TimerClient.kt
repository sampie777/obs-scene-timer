package nl.sajansen.obsscenetimer.remotesync.client

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

object TimerClient {
    private val logger = LoggerFactory.getLogger(TimerClient::class.java.name)

    private val websocketClient = WebSocketClient()
    private val timerClientSocket = TimerClientSocket({ onConnect() }, { onClose() })
    private var session: Session? = null
    private var reconnecting: Boolean = false
    private var connectionState: ConnectionState = ConnectionState.NOT_CONNECTED
    fun getConnectionState() = connectionState

    fun connect(url: String) {
        logger.info("Starting remote server connection")
        updateConnectionState(if (!reconnecting) ConnectionState.CONNECTING else ConnectionState.RECONNECTING)

        if (!Config.remoteSyncClientEnabled) {
            disconnect()
            return
        }

        try {
            websocketClient.start()
            val connection = websocketClient.connect(timerClientSocket, URI(url), ClientUpgradeRequest())
            session = connection.get()
            logger.info("Connection started")
        } catch (e: Exception) {
            logger.error("Failed to start connection. ${e.localizedMessage}")
            e.printStackTrace()
            processFailedConnection(
                "Could not connect to remote server ($url): ${e.localizedMessage}",
                reconnect = true
            )
            return
        }

        reconnecting = false
    }

    fun disconnect() {
        logger.info("Disconnecting form remote server")
        try {
            session?.close()
            timerClientSocket.disconnect()
        } catch (e: Exception) {
            logger.warn("Failed to close connection session")
            e.printStackTrace()
        }

        try {
            websocketClient.stop()
        } catch (e: Exception) {
            logger.warn("Failed to close client")
            e.printStackTrace()
        }

        logger.info("Disconnected from remote server")
        updateConnectionState(ConnectionState.NOT_CONNECTED)
    }

    private fun processFailedConnection(message: String, reconnect: Boolean = true) {
        updateConnectionState(ConnectionState.CONNECTION_FAILED)

        if (!reconnecting) {
            Notifications.add(message, "Scene Timer")
        }

        if (reconnect) {
            startReconnectingTimeout()
        }
    }

    private fun startReconnectingTimeout() {
        val connectionRetryTimer = Timer()
        connectionRetryTimer.schedule(object : TimerTask() {
            override fun run() {
                reconnecting = true
                connect(Config.remoteSyncClientAddress)
            }
        }, Config.remoteSyncClientReconnectionTimeout)
    }

    private fun updateConnectionState(state: ConnectionState) {
        connectionState = state
        RemoteSyncRefreshableRegister.remoteSyncClientRefreshConnectionState(state)
    }

    private fun onConnect() {
        updateConnectionState(ConnectionState.CONNECTED)
    }

    private fun onClose() {
        updateConnectionState(ConnectionState.DISCONNECTED)
        startReconnectingTimeout()
    }
}