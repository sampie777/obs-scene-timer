package nl.sajansen.obsscenetimer.gui

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.obs.OBSConnectionStatus
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.resetConfig
import nl.sajansen.obsscenetimer.themes.Theme
import kotlin.test.*

class TimerPanelTest {

    @BeforeTest
    fun before() {
        resetConfig()
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.setMaxTimerValue(0)
        Config.timerFlashForRemainingTimeLessThan = 0
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
        OBSState.connectionStatus = OBSConnectionStatus.DISCONNECTED
        val panel = TimerPanel()

        assertTrue(panel.sceneLabel.isVisible)
        assertEquals("Waiting for connection...", panel.sceneLabel.text)

        OBSState.connectionStatus = OBSConnectionStatus.CONNECTED
        OBSState.currentScene = TScene("scene1")
        panel.switchedScenes()

        assertEquals("scene1", panel.sceneLabel.text)

        OBSState.currentScene = TScene("scene2")
        panel.switchedScenes()

        assertEquals("scene2", panel.sceneLabel.text)
    }
}