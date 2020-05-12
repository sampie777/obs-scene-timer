package objects

import GUI
import GuiComponentMock
import config.Config
import net.twasi.obsremotejava.objects.Scene
import java.io.File
import kotlin.test.*

class OBSClientTest {

    @BeforeTest
    fun before() {
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSState.currentSceneName = ""
        OBSState.scenes.clear()
    }

    @Test
    fun testProcessNewScene() {
        val panelMock = GuiComponentMock()
        GUI.register(panelMock)
        OBSSceneTimer.increase()   // 1

        assertFalse(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertEquals(1, OBSSceneTimer.getValue())

        // When
        OBSClient.processNewScene("scene1")

        assertFalse(panelMock.refreshScenesCalled)
        assertTrue(panelMock.switchedScenesCalled)
        assertTrue(panelMock.refreshTimerCalled)
        assertEquals("scene1", OBSState.currentSceneName)
        assertEquals(0, OBSSceneTimer.getValue())
    }

    @Test
    fun testSetOBSScenes() {
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES
        val panelMock = GuiComponentMock()
        GUI.register(panelMock)

        Config.obsAddress = "ws://somewhereNotLocalhost"

        assertFalse(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertFalse(panelMock.refreshOBSStatusCalled)

        val scenes = ArrayList<Scene>()
        scenes.add(Scene())
        scenes.add(Scene())
        scenes.add(Scene())

        // When
        OBSClient.processOBSScenesToOBSStateScenes(scenes)

        assertTrue(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertTrue(panelMock.refreshOBSStatusCalled)
        assertEquals(3, OBSState.scenes.size)
        assertNull(OBSState.clientActivityStatus)
    }

    @Test
    fun testGetVideoLength() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath

        assertEquals(2, OBSClient.getVideoLength(filename))
    }

    @Test
    fun testGetVideoLengthForNonExistingVideo() {
        val filename = "nonexistingfile"

        assertEquals(0, OBSClient.getVideoLength(filename))
    }
}