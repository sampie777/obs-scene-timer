package nl.sajansen.obsscenetimer.mocks

import nl.sajansen.obsscenetimer.gui.Refreshable

class GuiComponentMock : Refreshable {
    var refreshTimerCalled: Boolean = false
    var switchedScenesCalled: Boolean = false
    var refreshScenesCalled: Boolean = false
    var refreshOBSStatusCalled: Boolean = false
    var refreshNotificationsCalled: Boolean = false

    override fun refreshTimer() {
        refreshTimerCalled = true
    }

    override fun switchedScenes() {
        switchedScenesCalled = true
    }

    override fun refreshScenes() {
        refreshScenesCalled = true
    }

    override fun refreshOBSStatus() {
        refreshOBSStatusCalled = true
    }

    override fun refreshNotifications() {
        refreshNotificationsCalled = true
    }

    fun resetCalleds() {
        refreshTimerCalled = false
        switchedScenesCalled = false
        refreshScenesCalled = false
        refreshOBSStatusCalled = false
        refreshNotificationsCalled = false
    }
}