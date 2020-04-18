package gui

import config.Config
import objects.OBSSceneTimer
import kotlin.test.Test
import kotlin.test.assertEquals

class TimerPanelTest {

    @Test
    fun testChangingBackgroundColorForTimer() {
        val panel = TimerPanel()
        Config.smallMinLimitForLimitApproaching = 0
        Config.smallTimeDifferenceForLimitApproaching = 1
        Config.largeMinLimitForLimitApproaching = 100
        OBSSceneTimer.resetTimer()
        OBSSceneTimer.setMaxTimerValue(3)

        OBSSceneTimer.increaseTimer()

        panel.refreshTimer()
        assertEquals(Config.timerBackgroundColor, panel.background)

        OBSSceneTimer.increaseTimer()

        panel.refreshTimer()
        assertEquals(Config.approachingLimitColor, panel.background)

        OBSSceneTimer.increaseTimer()

        panel.refreshTimer()
        assertEquals(Config.exceededLimitColor, panel.background)
    }
}