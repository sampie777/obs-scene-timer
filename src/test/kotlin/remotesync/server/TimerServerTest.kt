package remotesync.server

import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.remotesync.server.TimerServer
import org.junit.Before
import resetConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TimerServerTest {

    @Before
    fun before() {
        resetConfig()
    }

    @Test
    fun testGetCurrentTimerMessage() {
        OBSState.currentScene = TScene("scenename")
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