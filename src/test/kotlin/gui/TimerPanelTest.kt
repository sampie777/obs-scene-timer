package gui

import config.Config
import objects.OBSClientStatus
import objects.OBSSceneTimer
import objects.OBSState
import themes.Theme
import kotlin.test.*

class TimerPanelTest {

    @BeforeTest
    fun before() {
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.setMaxTimerValue(0)
    }

    @Test
    fun testChangingBackgroundColorForTimer() {
        Theme.init()
        val panel = TimerPanel()
        Config.smallMinLimitForLimitApproaching = 0
        Config.smallTimeDifferenceForLimitApproaching = 1
        Config.largeMinLimitForLimitApproaching = 100
        OBSSceneTimer.setMaxTimerValue(3)

        OBSSceneTimer.increase()   // 1
        panel.refreshTimer()

        assertEquals(Theme.get.BACKGROUND_COLOR, panel.background)

        OBSSceneTimer.increase()   // 2
        panel.refreshTimer()

        assertEquals(Theme.get.TIMER_APPROACHING_BACKGROUND_COLOR, panel.background)

        OBSSceneTimer.increase()   // 3
        panel.refreshTimer()

        assertEquals(Theme.get.TIMER_EXCEEDED_BACKGROUND_COLOR, panel.background)
    }

    @Test
    fun testTimerPanelDisplaysCorrectTime() {
        val panel = TimerPanel()

        assertTrue(panel.timerUpLabel.isVisible)
        assertEquals("0:00:00", panel.timerUpLabel.text)
        assertFalse(panel.timerDownLabel.isVisible)

        OBSSceneTimer.increase()   // 1
        panel.refreshTimer()

        assertEquals("0:00:01", panel.timerUpLabel.text)
        assertFalse(panel.timerDownLabel.isVisible)

        OBSSceneTimer.increase()   // 2
        OBSSceneTimer.setMaxTimerValue(3)
        panel.refreshTimer()

        assertEquals("0:00:02", panel.timerUpLabel.text)
        assertTrue(panel.timerDownLabel.isVisible)
        assertEquals("0:00:01", panel.timerDownLabel.text)

        OBSSceneTimer.increase()   // 3
        panel.refreshTimer()

        assertEquals("0:00:03", panel.timerUpLabel.text)
        assertTrue(panel.timerDownLabel.isVisible)
        assertEquals("0:00:00", panel.timerDownLabel.text)

        OBSSceneTimer.increase()   // 4
        panel.refreshTimer()

        assertEquals("0:00:04", panel.timerUpLabel.text)
        assertTrue(panel.timerDownLabel.isVisible)
        assertEquals("-0:00:01", panel.timerDownLabel.text)
    }

    @Test
    fun testTimerPanelDisplaysCorrectScene() {
        OBSState.connectionStatus = OBSClientStatus.DISCONNECTED
        val panel = TimerPanel()

        assertTrue(panel.sceneLabel.isVisible)
        assertEquals("Waiting for connection...", panel.sceneLabel.text)

        OBSState.connectionStatus = OBSClientStatus.CONNECTED
        OBSState.currentSceneName = "scene1"
        panel.switchedScenes()

        assertEquals("scene1", panel.sceneLabel.text)

        OBSState.currentSceneName = "scene2"
        panel.switchedScenes()

        assertEquals("scene2", panel.sceneLabel.text)
    }
}