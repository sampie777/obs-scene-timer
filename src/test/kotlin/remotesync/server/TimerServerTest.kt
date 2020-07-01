package remotesync.server

import objects.OBSSceneTimer
import objects.OBSState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TimerServerTest {

    @Test
    fun testGetCurrentTimerMessage() {
        OBSState.currentSceneName = "scenename"
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()    // Set value to 1
        OBSSceneTimer.setMaxTimerValue(100)

        val message = TimerServer.getCurrentTimerMessage()

        assertEquals("scenename", message.sceneName)
        assertTrue(message.isTimed)
        assertEquals(1, message.elapsedTimeRaw)
        assertEquals(99, message.remainingTimeRaw)
        assertEquals("0:01:39", message.remainingTime)
        assertEquals(100, message.maximumTime)
    }

}