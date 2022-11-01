package nl.sajansen.obsscenetimer.obs

enum class OBSClientStatus(val status: String) {
    LOADING_SCENES("Loading scenes..."),
    LOADING_SCENE_SOURCES("Loading scene sources..."),
}