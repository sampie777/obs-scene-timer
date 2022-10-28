package obs

import GUI
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import config.Config
import io.obswebsocket.community.client.model.Scene
import mocks.GetInputSettingsResponseMock
import mocks.GuiComponentMock
import mocks.SceneMock
import objects.*
import objects.notifications.Notifications
import resetConfig
import java.io.File
import kotlin.test.*

class ObsSceneProcessorTest {

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
    fun testProcessNewScene() {
        val panelMock = GuiComponentMock()
        GUI.register(panelMock)
        OBSSceneTimer.increase()   // 1

        assertFalse(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertEquals(1, OBSSceneTimer.getValue())

        // When
        ObsSceneProcessor.processNewScene("scene1")

        assertFalse(panelMock.refreshScenesCalled)
        assertTrue(panelMock.switchedScenesCalled)
        assertTrue(panelMock.refreshTimerCalled)
        assertEquals("scene1", OBSState.currentScene.name)
        assertEquals(0, OBSSceneTimer.getValue())
    }

    @Test
    fun `test processing OBS scenes for non localhost processes only scene names`() {
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES
        val panelMock = GuiComponentMock()
        GUI.register(panelMock)

        Config.obsAddress = "ws://somewhereNotLocalhost"

        assertFalse(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertFalse(panelMock.refreshOBSStatusCalled)

        val scenes = ArrayList<Scene>()
        scenes.add(SceneMock("Scene 1"))
        scenes.add(SceneMock("Scene 2", 0))
        scenes.add(SceneMock("Scene 3"))

        // When
        ObsSceneProcessor.processOBSScenesToOBSStateScenes(scenes)

        assertTrue(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertTrue(panelMock.refreshOBSStatusCalled)
        assertEquals(3, OBSState.scenes.size)
        assertNull(OBSState.clientActivityStatus)

        assertEquals("Scene 1", OBSState.scenes[0].name)
        assertEquals(0, OBSState.scenes[0].sources.size)
        assertEquals(0, OBSState.scenes[1].sources.size)
        assertEquals("Scene 3", OBSState.scenes[2].name)
        assertEquals(0, OBSState.scenes[2].sources.size)
    }

    @Test
    fun testAutoCalculateSceneLimitsBySourcesPreventsSourceLoading() {
        Config.autoCalculateSceneLimitsBySources = false
        val scenes = ArrayList<Scene>()
        scenes.add(SceneMock("Scene 1"))
        scenes.add(SceneMock("Scene 2"))
        scenes.add(SceneMock("Scene 3"))

        // When
        ObsSceneProcessor.processOBSScenesToOBSStateScenes(scenes)

        assertEquals(0, OBSState.scenes[0].sources.size)
        assertEquals(0, OBSState.scenes[1].sources.size)
        assertEquals(0, OBSState.scenes[2].sources.size)
    }

    @Test
    fun testAutoCalculateSceneLimitsBySourcesPreventsSourceSettingsLoading() {
        Config.autoCalculateSceneLimitsBySources = false
        Config.obsAddress = "ws://123.123.123.123:0"

        assertFalse(ObsSceneProcessor.loadSourceSettingsForAllScenes())
    }

    @Test
    fun testGetSourceLengthForMediaSourceWithValidSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("Media source 1", "ffmpeg_source")
        source.file = TVideoFile(name = filename)

        ObsSceneProcessor.getSourceLengthForMediaSource(source)

        assertEquals(filename, source.file!!.name)
        assertEquals(2, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithValidSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.playlist = TPlayList(listOf(TVideoFile(name = filename)))

        ObsSceneProcessor.getSourceLengthForVLCVideoSource(source)

        assertEquals(filename, source.file!!.name)
        assertEquals(2, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMultipleFilesAndChooseLongest() {
        Config.sumVlcPlaylistSourceLengths = false
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.playlist = TPlayList(listOf(
            TVideoFile(name = filename),
            TVideoFile(name = filename),
        ))

        ObsSceneProcessor.getSourceLengthForVLCVideoSource(source)

        assertEquals(filename, source.file!!.name)
        assertEquals(2, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMultipleFilesAndSumVideos() {
        Config.sumVlcPlaylistSourceLengths = true
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.playlist = TPlayList(listOf(
            TVideoFile(name = filename),
            TVideoFile(name = filename),
        ))

        ObsSceneProcessor.getSourceLengthForVLCVideoSource(source)

        assertEquals(filename, source.file!!.name)
        assertEquals(4, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun `test get source length for playlist with an empty file name`() {
        val source = TSource("VLC source 1", "vlc_source")
        source.playlist = TPlayList(listOf(
            TVideoFile(),
        ))

        ObsSceneProcessor.getSourceLengthForVLCVideoSource(source)

        assertEquals("", source.file!!.name)
        assertEquals(0, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMissingPlaylistItemValueProperty() {
        val source = TSource("VLC source 1", "vlc_source")

        val responsePlaylistEntry = JsonObject()
        responsePlaylistEntry.addProperty("hidden", false)
        responsePlaylistEntry.addProperty("selected", false)
        val responsePlaylist = JsonArray()
        responsePlaylist.add(responsePlaylistEntry)

        val response = GetInputSettingsResponseMock(
            inputKind = "vlc_source",
            inputSettings = JsonObject().also { it.add("playlist", responsePlaylist) })

        ObsSceneProcessor.processGetInputSettingsResponse(source, response)

        assertEquals("", source.file!!.name)
        assertEquals(0, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun `test get video length for empty playlist`() {
        val source = TSource("VLC source 1", "vlc_source")
        val response = GetInputSettingsResponseMock(
            inputKind = "vlc_source",
            inputSettings = JsonObject().also { it.add("playlist", JsonArray()) })

        ObsSceneProcessor.processGetInputSettingsResponse(source, response)

        assertEquals("", source.file!!.name)
        assertEquals(0, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testAssignSourceSettingsFromOBSResponseWithVLCSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", kind = "vlc_source")

        val responsePlaylistEntry = JsonObject()
        responsePlaylistEntry.addProperty("hidden", false)
        responsePlaylistEntry.addProperty("selected", false)
        responsePlaylistEntry.addProperty("value", filename)
        val responsePlaylist = JsonArray()
        responsePlaylist.add(responsePlaylistEntry)

        val response = GetInputSettingsResponseMock(
            inputKind = "vlc_source",
            inputSettings = JsonObject().also { it.add("playlist", responsePlaylist) })

        ObsSceneProcessor.processGetInputSettingsResponse(source, response)

        assertEquals("VLC source 1", source.name)   // Doesn't change
        assertEquals(1, source.playlist!!.entries.size)
        assertEquals(filename, source.file!!.name)
        assertEquals(2, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testAssignSourceSettingsFromOBSResponseWithFFMPEGSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("Media source 1", kind = "ffmpeg_source")
        val response = GetInputSettingsResponseMock(
            inputKind = "ffmpeg_source",
            inputSettings = JsonObject().also { it.addProperty("local_file", filename) })

        ObsSceneProcessor.processGetInputSettingsResponse(source, response)

        assertEquals("Media source 1", source.name)   // Doesn't change
        assertEquals(filename, source.file!!.name)
        assertEquals(2, source.file!!.duration)
        assertEquals(0, Notifications.list.size)
    }
}