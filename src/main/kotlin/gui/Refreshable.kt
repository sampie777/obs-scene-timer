package gui

import java.awt.Component

interface Refreshable {
    fun refreshTimer() {}
    fun switchedScenes() {}
    fun refreshScenes() {}

    fun refreshOBSStatus() {}

    fun refreshNotifications() {}

    fun windowClosing(window: Component?) {}
}