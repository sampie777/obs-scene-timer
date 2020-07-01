package remotesync.client

import objects.OBSSceneTimer
import objects.OBSState
import org.eclipse.jetty.websocket.api.*
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals


class TimerClientSocketTest {

    @Test
    fun testMessageProcessing(){
        OBSSceneTimer.timerMessage = null
        OBSState.currentSceneName = "current scene"

        val message = "{\"sceneName\":\"scenename\",\"elapsedTime\":\"00:00:10\",\"elapsedTimeRaw\":10,\"timerState\":\"NEUTRAL\",\"isTimed\":true,\"remainingTime\":\"00:00:20\",\"remainingTimeRaw\":20,\"maximumTime\":30,\"timestamp\":\"2020-06-04T16:49:58.670Z\",\"messageType\":\"TimerMessage\"}"
        val socket = TimerClientSocket({}, {})

        socket.onTextMessage(message)

        assertEquals("scenename", OBSSceneTimer.timerMessage?.sceneName)
        assertEquals(10, OBSSceneTimer.timerMessage?.elapsedTimeRaw)
        assertEquals("scenename", OBSState.currentSceneName)
    }

}