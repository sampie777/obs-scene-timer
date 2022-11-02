package nl.sajansen.obsscenetimer.remotesync.server


import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import org.slf4j.LoggerFactory

class EventServlet : WebSocketServlet() {
    private val logger = LoggerFactory.getLogger(EventServlet::class.java.name)

    override fun configure(factory: WebSocketServletFactory) {
        factory.register(EventSocket::class.java)
    }
}