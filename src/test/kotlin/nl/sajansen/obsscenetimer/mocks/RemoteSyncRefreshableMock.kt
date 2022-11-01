package nl.sajansen.obsscenetimer.mocks


import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import java.util.logging.Logger

class RemoteSyncRefreshableMock : RemoteSyncRefreshable {
    private val logger = Logger.getLogger(RemoteSyncRefreshableMock::class.java.name)

    var remoteSyncClientRefreshConnectionState = false
    var remoteSyncServerRefreshConnectionState = false
    var remoteSyncServerConnectionsUpdate = false

    override fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        remoteSyncClientRefreshConnectionState = true
    }

    override fun remoteSyncServerRefreshConnectionState() {
        remoteSyncServerRefreshConnectionState = true
    }

    override fun remoteSyncServerConnectionsUpdate() {
        remoteSyncServerConnectionsUpdate = true
    }
}