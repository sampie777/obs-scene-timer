package nl.sajansen.obsscenetimer.gui

import nl.sajansen.obsscenetimer.objects.TScene
import java.awt.Component

interface Refreshable {
    fun refreshTimer() {}
    fun switchedScenes() {}
    fun refreshScenes() {}
    fun onSceneTimeLimitUpdated(scene: TScene) {}
    fun refreshGroups() {}

    fun refreshOBSStatus() {}

    fun refreshNotifications() {}

    fun windowClosing(window: Component?) {}
}