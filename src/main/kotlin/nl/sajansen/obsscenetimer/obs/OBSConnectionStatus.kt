package nl.sajansen.obsscenetimer.obs

enum class OBSConnectionStatus(val status: String) {
    UNKNOWN("Unknown"),
    CONNECTED("Connected"),
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting..."),
    RECONNECTING("Reconnecting..."),
    CONNECTION_FAILED("Connection failed!"),

    LOADING_SCENES("Loading scenes..."),
    LOADING_SCENE_SOURCES("Loading scene sources..."),
}
