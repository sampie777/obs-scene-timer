package obs

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSClient
import nl.sajansen.obsscenetimer.obs.OBSState
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