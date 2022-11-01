package nl.sajansen.obsscenetimer.remotesync.objects

interface RemoteSyncRefreshable {
    fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {}
    fun remoteSyncServerRefreshConnectionState() {}
    fun remoteSyncServerConnectionsUpdate() {}
}