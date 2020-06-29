package remotesync.server

import org.eclipse.jetty.websocket.api.Session

object ServerStatus {
    val clients = hashMapOf<String, Session>()
}