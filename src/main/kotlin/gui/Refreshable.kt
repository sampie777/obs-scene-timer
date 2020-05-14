package gui

interface Refreshable {
    fun refreshTimer() {}
    fun switchedScenes() {}
    fun refreshScenes() {}

    fun refreshOBSStatus() {}

    fun refreshNotifications() {}

    fun windowClosing() {}
}