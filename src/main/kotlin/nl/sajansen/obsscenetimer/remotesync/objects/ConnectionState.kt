package nl.sajansen.obsscenetimer.remotesync.objects

enum class ConnectionState(val text: String) {
    NOT_CONNECTED("Not connected"),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected"),
    CONNECTION_FAILED("Connection failed"),
    RECONNECTING("Reconnecting...")
}