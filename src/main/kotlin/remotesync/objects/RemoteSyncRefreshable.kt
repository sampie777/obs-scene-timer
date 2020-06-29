package remotesync.objects

interface RemoteSyncRefreshable {
    fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {}
    fun remoteSyncServerRefreshConnectionState() {}
}