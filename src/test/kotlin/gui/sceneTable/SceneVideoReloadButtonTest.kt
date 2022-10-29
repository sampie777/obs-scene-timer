package gui.sceneTable

import config.Config
import objects.TScene
import objects.TSource
import objects.TVideoFile
import obs.OBSClientStatus
import obs.OBSState
import org.junit.Before
import org.junit.Test
import resetConfig
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SceneVideoReloadButtonTest {
    @Before
    fun before() {
        resetConfig()
        OBSState.clientActivityStatus = null
    }

    @Test
    fun `test button state when all is loaded`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true

        val button = SceneVideoReloadButton(scene)

        assertEquals("Reset time to max video length", button.toolTipText)
        assertTrue(button.isEnabled)
    }

    @Test
    fun `test button state when scene is loading 1`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES

        val button = SceneVideoReloadButton(scene)

        assertEquals("Calculating media length...", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when scene is loading 2`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES

        val button = SceneVideoReloadButton(scene)

        assertEquals("Calculating media length...", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when scene is loading when time is equal to max time`() {
        val scene = TScene()
        scene.timeLimit = 10
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES

        val button = SceneVideoReloadButton(scene)

        assertEquals("Calculating media length...", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when time is equal to max time with time limit specified`() {
        val scene = TScene()
        scene.timeLimit = 10
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true

        val button = SceneVideoReloadButton(scene)

        assertEquals("Time is already equal to max video length", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when time is equal to max time without time limit specified`() {
        val scene = TScene()
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true

        val button = SceneVideoReloadButton(scene)

        assertEquals("Time is already equal to max video length", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when scenes are not loaded`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = false

        val button = SceneVideoReloadButton(scene)

        assertEquals("Please reload the scenes (Application > Reload scenes) to calculate max scene time", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when OBS is not on localhost`() {
        Config.obsAddress = "ws://123.123.123:1234"
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = false

        val button = SceneVideoReloadButton(scene)

        assertEquals("Make sure OBS is running on the same computer (localhost) to use this feature", button.toolTipText)
        assertFalse(button.isEnabled)
    }

    @Test
    fun `test button state when sources are loaded but OBS is not on localhost`() {
        Config.obsAddress = "ws://123.123.123:1234"
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true

        val button = SceneVideoReloadButton(scene)

        assertEquals("Make sure OBS is running on the same computer (localhost) to use this feature", button.toolTipText)
        assertFalse(button.isEnabled)
    }
}