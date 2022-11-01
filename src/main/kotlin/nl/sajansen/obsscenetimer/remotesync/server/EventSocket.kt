package nl.sajansen.obsscenetimer.remotesync.server


import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.common.WebSocketSession
import java.util.logging.Logger

class EventSocket : WebSocketAdapter() {
    private val logger = Logger.getLogger(EventSocket::class.java.name)

    private var sessionId: String = ""

    override fun onWebSocketConnect(session: Session) {
        logger.info("Socket Connected: $session")
        super.onWebSocketConnect(session)

        sessionId = (this.session as WebSocketSession).connection.id
        ServerStatus.clients[sessionId] = session

        logger.info("Currently ${ServerStatus.clients.size} clients connected")
        RemoteSyncRefreshableRegister.remoteSyncServerConnectionsUpdate()
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        logger.info("Socket Closed: [$statusCode] $reason")

        super.onWebSocketClose(statusCode, reason)
        ServerStatus.clients.remove(sessionId)

        logger.info("Currently ${ServerStatus.clients.size} clients connected")
        RemoteSyncRefreshableRegister.remoteSyncServerConnectionsUpdate()
    }

    override fun onWebSocketError(cause: Throwable) {
        super.onWebSocketError(cause)
        cause.printStackTrace()
    }
}