package objects

import GUI
import com.google.gson.Gson
import config.Config
import mocks.GuiComponentMock
import mocks.SceneMockWithSources
import net.twasi.obsremotejava.objects.Scene
import net.twasi.obsremotejava.requests.GetSourceSettings.GetSourceSettingsResponse
import objects.notifications.Notifications
import java.io.File
import kotlin.test.*

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
        assertEquals("scene1", OBSState.currentScene.name)
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
        scenes.add(SceneMockWithSources("Scene 1"))
        scenes.add(Scene())
        scenes.add(SceneMockWithSources("Scene 3"))

        // When
        OBSClient.processOBSScenesToOBSStateScenes(scenes)

        assertTrue(panelMock.refreshScenesCalled)
        assertFalse(panelMock.switchedScenesCalled)
        assertFalse(panelMock.refreshTimerCalled)
        assertTrue(panelMock.refreshOBSStatusCalled)
        assertEquals(3, OBSState.scenes.size)
        assertNull(OBSState.clientActivityStatus)

        assertEquals("Scene 1", OBSState.scenes[0].name)
        assertEquals(1, OBSState.scenes[0].sources.size)
        assertEquals("Scene 1 source", OBSState.scenes[0].sources[0].name)
        assertEquals(0, OBSState.scenes[1].sources.size)
        assertEquals("Scene 3", OBSState.scenes[2].name)
        assertEquals(1, OBSState.scenes[2].sources.size)
    }

    @Test
    fun testAutoCalculateSceneLimitsBySourcesPreventsSourceLoading() {
        Config.autoCalculateSceneLimitsBySources = false
        val scenes = ArrayList<Scene>()
        scenes.add(SceneMockWithSources("Scene 1"))
        scenes.add(Scene())
        scenes.add(SceneMockWithSources("Scene 3"))

        // When
        OBSClient.processOBSScenesToOBSStateScenes(scenes)

        assertEquals(0, OBSState.scenes[0].sources.size)
        assertEquals(0, OBSState.scenes[1].sources.size)
        assertEquals(0, OBSState.scenes[2].sources.size)
    }

    @Test
    fun testAutoCalculateSceneLimitsBySourcesPreventsSourceSettingsLoading() {
        Config.autoCalculateSceneLimitsBySources = false
        Config.obsAddress = "ws://123.123.123.123:0"

        assertFalse(OBSClient.loadSourceSettings())
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

    @Test
    fun testNotRunningIfRemoteSyncClientEnabled() {
        Config.remoteSyncClientEnabled = true

        OBSClient.start()

        assertFalse(OBSClient.isRunning())
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithValidSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.settings = mapOf(
            "playlist" to listOf(
                mapOf(
                    "hidden" to false,
                    "selected" to false,
                    "value" to filename
                )
            )
        )

        OBSClient.getSourceLengthForVLCSource(source)

        assertEquals(filename, source.fileName)
        assertEquals(2, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMultipleFilesAndChooseLongest() {
        Config.sumVlcPlaylistSourceLengths = false
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.settings = mapOf(
            "playlist" to listOf(
                mapOf(
                    "hidden" to false,
                    "selected" to false,
                    "value" to filename
                ),
                mapOf(
                    "hidden" to false,
                    "selected" to false,
                    "value" to filename
                )
            )
        )

        OBSClient.getSourceLengthForVLCSource(source)

        assertEquals(filename, source.fileName)
        assertEquals(2, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMultipleFilesAndSumVideos() {
        Config.sumVlcPlaylistSourceLengths = true
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1", "vlc_source")
        source.settings = mapOf(
            "playlist" to listOf(
                mapOf(
                    "hidden" to false,
                    "selected" to false,
                    "value" to filename
                ),
                mapOf(
                    "hidden" to false,
                    "selected" to false,
                    "value" to filename
                )
            )
        )

        OBSClient.getSourceLengthForVLCSource(source)

        assertEquals(filename, source.fileName)
        assertEquals(4, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithInvalidPlaylistProperty() {
        val source = TSource("VLC source 1", "vlc_source")
        source.settings = mapOf(
            "playlist" to ""
        )

        OBSClient.getSourceLengthForVLCSource(source)

        assertEquals("", source.fileName)
        assertEquals(0, source.videoLength)
        assertEquals(1, Notifications.list.size)
        assertEquals("Could not load duration for VLC source 'VLC source 1'", Notifications.list.first().message)
    }

    @Test
    fun testGetSourceLengthForVLCSourceWithMissingPlaylistItemValueProperty() {
        val source = TSource("VLC source 1", "vlc_source")
        source.settings = mapOf(
            "playlist" to listOf(
                mapOf(
                    "hidden" to false,
                    "selected" to false
                )
            )
        )

        OBSClient.getSourceLengthForVLCSource(source)

        assertEquals("", source.fileName)
        assertEquals(0, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testAssignSourceSettingsFromOBSResponseWithVLCSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("VLC source 1")
        val response = Gson().fromJson(
            """
{
    "sourceName": "VLC source 1",
    "sourceType": "vlc_source",
    "sourceSettings": {
        "playlist": [
            {
                "hidden": false,
                "selected": false,
                "value": "$filename"
            }
        ]
    }
}
        """.trimIndent(), GetSourceSettingsResponse::class.java
        )

        OBSClient.assignSourceSettingsFromOBSResponse(source, response)

        assertEquals("VLC source 1", source.name)   // Doesn't change
        assertTrue(source.settings.containsKey("playlist"))
        assertEquals(filename, source.fileName)
        assertEquals(2, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }

    @Test
    fun testAssignSourceSettingsFromOBSResponseWithFFMPEGSource() {
        val filename = File(javaClass.classLoader.getResource("video2seconds.mkv")!!.file).absolutePath
        val source = TSource("Media source 1")
        val response = Gson().fromJson(
            """
{
    "sourceName": "Media source 1",
    "sourceType": "ffmpeg_source",
    "sourceSettings": {
        "local_file": "$filename"
    }
}
        """.trimIndent(), GetSourceSettingsResponse::class.java
        )

        OBSClient.assignSourceSettingsFromOBSResponse(source, response)

        assertEquals("Media source 1", source.name)   // Doesn't change
        assertTrue(source.settings.containsKey("local_file"))
        assertEquals(filename, source.fileName)
        assertEquals(2, source.videoLength)
        assertEquals(0, Notifications.list.size)
    }
}