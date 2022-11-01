package nl.sajansen.obsscenetimer.remotesync.server


import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import java.util.logging.Logger

class EventServlet : WebSocketServlet() {
    private val logger = Logger.getLogger(EventServlet::class.java.name)

    override fun configure(factory: WebSocketServletFactory) {
        factory.register(EventSocket::class.java)
    }
}