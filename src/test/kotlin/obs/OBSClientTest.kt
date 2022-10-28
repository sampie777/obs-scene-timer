package obs

import config.Config
import objects.OBSSceneTimer
import objects.TScene
import objects.notifications.Notifications
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

class OBSClientTest {

    @BeforeTest
    fun before() {
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSState.currentScene = TScene("")
        OBSState.scenes.clear()
        Config.autoCalculateSceneLimitsBySources = true
        Notifications.clear()
    }

    @Test
    fun testNotRunningIfRemoteSyncClientEnabled() {
        Config.remoteSyncClientEnabled = true

        OBSClient.start()

        assertFalse(OBSClient.isRunning())
    }
}