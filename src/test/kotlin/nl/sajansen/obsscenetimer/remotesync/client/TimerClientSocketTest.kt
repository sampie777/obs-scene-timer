package nl.sajansen.obsscenetimer.remotesync.client

import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.obs.OBSState
import kotlin.test.Test
import kotlin.test.assertEquals


class TimerClientSocketTest {

    @Test
    fun testMessageProcessing(){
        OBSSceneTimer.timerMessage = null
        OBSState.currentScene = TScene("current scene")

        val message = "{\"sceneName\":\"scenename\",\"elapsedTime\":\"00:00:10\",\"elapsedTimeRaw\":10,\"timerState\":\"NEUTRAL\",\"isTimed\":true,\"remainingTime\":\"00:00:20\",\"remainingTimeRaw\":20,\"maximumTime\":30,\"timestamp\":\"2020-06-04T16:49:58.670Z\",\"messageType\":\"TimerMessage\"}"
        val socket = TimerClientSocket({}, {})

        socket.onTextMessage(message)

        assertEquals("scenename", OBSSceneTimer.timerMessage?.sceneName)
        assertEquals(10, OBSSceneTimer.timerMessage?.elapsedTimeRaw)
        assertEquals("scenename", OBSState.currentScene.name)
    }

}