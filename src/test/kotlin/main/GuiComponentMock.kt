package main

import gui.Refreshable

class GuiComponentMock : Refreshable {
    var refreshTimerCalled: Boolean = false
    var switchedScenesCalled: Boolean = false
    var refreshScenesCalled: Boolean = false
    var refreshOBSStatusCalled: Boolean = false

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

    fun resetCalleds() {
        refreshTimerCalled = false
        switchedScenesCalled = false
        refreshScenesCalled = false
        refreshOBSStatusCalled = false
    }
}