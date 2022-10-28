package obs

import config.Config
import objects.OBSSceneTimer
import objects.TScene
import objects.notifications.Notifications
import resetConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

class OBSClientTest {

    @BeforeTest
    fun before() {
        resetConfig()
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSState.currentScene = TScene("")
        OBSState.scenes.clear()
        Notifications.clear()
    }

    @Test
    fun testNotRunningIfRemoteSyncClientEnabled() {
        Config.remoteSyncClientEnabled = true

        OBSClient.start()

        assertFalse(OBSClient.isRunning())
    }
}