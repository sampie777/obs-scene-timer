package gui.sceneTable

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.sceneTable.SceneInputPanel
import nl.sajansen.obsscenetimer.gui.sceneTable.SceneVideoLoadingIcon
import nl.sajansen.obsscenetimer.gui.sceneTable.SceneVideoReloadButton
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.objects.TSource
import nl.sajansen.obsscenetimer.objects.TVideoFile
import nl.sajansen.obsscenetimer.obs.OBSClientStatus
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.utils.FAIcon
import org.junit.Before
import org.junit.Test
import resetConfig
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SceneInputPanelTest {
    @Before
    fun before() {
        resetConfig()
        OBSState.clientActivityStatus = null
    }

    @Test
    fun `test component state when all is loaded and time differs`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10), settingsLoaded = true))
        scene.sourcesAreLoaded = true

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoReloadButton)
    }

    @Test
    fun `test component state when scene and sources is loaded and time differs`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10), settingsLoaded = true))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoReloadButton)
    }

    @Test
    fun `test component state when time is equal to max time with time limit specified`() {
        val scene = TScene()
        scene.timeLimit = 10
        scene.sources.add(TSource(file = TVideoFile(duration = 10), settingsLoaded = true))
        scene.sourcesAreLoaded = true

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoReloadButton)
    }

    @Test
    fun `test component state when time is equal to max time without time limit specified`() {
        val scene = TScene()
        scene.timeLimit = null
        scene.sources.add(TSource(file = TVideoFile(duration = 10), settingsLoaded = true))
        scene.sourcesAreLoaded = true

        val component = SceneInputPanel(scene)

        assertEquals(0, component.reloadPanel.componentCount)
    }

    @Test
    fun `test component state when scene sources are loading`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoLoadingIcon)
    }

    @Test
    fun `test component state when scenes are loading`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoLoadingIcon)
    }

    @Test
    fun `test component state when scene is loading`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = false
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENES

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoLoadingIcon)
    }

    @Test
    fun `test component state when scene is loading when time is equal to max time`() {
        val scene = TScene()
        scene.timeLimit = 10
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true
        OBSState.clientActivityStatus = OBSClientStatus.LOADING_SCENE_SOURCES

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is SceneVideoLoadingIcon)
    }

    @Test
    fun `test component state when scenes are not loaded`() {
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = false

        val component = SceneInputPanel(scene)

        assertEquals(1, component.reloadPanel.componentCount)
        assertTrue(component.reloadPanel.components[0] is FAIcon)
    }

    @Test
    fun `test component state when OBS is not on localhost`() {
        Config.obsAddress = "ws://123.123.123:1234"
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = false

        val component = SceneInputPanel(scene)

        assertEquals(0, component.reloadPanel.componentCount)
    }

    @Test
    fun `test component state when sources are loaded but OBS is not on localhost`() {
        Config.obsAddress = "ws://123.123.123:1234"
        val scene = TScene()
        scene.timeLimit = 9
        scene.sources.add(TSource(file = TVideoFile(duration = 10)))
        scene.sourcesAreLoaded = true

        val component = SceneInputPanel(scene)

        assertEquals(0, component.reloadPanel.componentCount)
    }
}