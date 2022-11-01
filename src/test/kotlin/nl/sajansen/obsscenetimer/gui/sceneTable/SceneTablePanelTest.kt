package nl.sajansen.obsscenetimer.gui.sceneTable

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.*
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.resetConfig
import java.awt.event.FocusEvent
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.test.*

class SceneTablePanelTest {

    @BeforeTest
    fun before() {
        resetConfig()
        OBSState.scenes.clear()
        OBSState.currentScene = TScene("")
        OBSSceneTimer.stop()
        OBSSceneTimer.setMaxTimerValue(0)
    }

    @Test
    fun testCreatingSceneInputsHashMapWithEmptyConfig() {
        val panel = SceneTablePanel()

        assertEquals(0, panel.sceneInputs.size)
        assertEquals(0, panel.sceneLabels.size)

        // When
        OBSState.scenes.add(TScene("scene1"))
        OBSState.scenes.add(TScene("scene2"))

        panel.refreshScenes()

        assertEquals(2, panel.sceneInputs.size)
        assertEquals(2, panel.sceneLabels.size)
    }

    @Test
    fun testCreatingSceneInputsHashMapWithFilledConfig() {
        Config.sceneProperties.tScenes.add(Json.TScene("scene1", timeLimit = 10, groups = emptySet()))
        OBSState.scenes.add(TScene("scene1"))
        val panel = SceneTablePanel()

        assertEquals(1, panel.sceneInputs.size)
        assertTrue(panel.sceneInputs.containsKey("scene1"), "SceneValues doesn't contain key 'scene1'")
        assertEquals("0:10", panel.sceneInputs["scene1"]!!.sceneInput.text)
        assertEquals(1, panel.sceneLabels.size)

        // When
        OBSState.scenes.add(TScene("scene2"))

        panel.refreshScenes()

        assertEquals(2, panel.sceneInputs.size)
        assertEquals(2, panel.sceneLabels.size)
    }

    @Test
    fun testCreatingCorrectSceneInputsHashMapWithFilledConfigAndMaxVideoSizeAvailable() {
        OBSState.scenes.add(TScene("scene1_value_set_in_config", timeLimit = 10))
        OBSState.scenes.add(TScene("scene4_with_maxvideo_but_value_set_in_config", timeLimit = 40))
        Config.save()
        val panel = SceneTablePanel()

        assertEquals(2, panel.sceneInputs.size)
        assertTrue(panel.sceneInputs.containsKey("scene1_value_set_in_config"), "Missing key in SceneInputs")
        assertEquals("0:10", panel.sceneInputs["scene1_value_set_in_config"]!!.sceneInput.text)
        assertTrue(panel.sceneInputs.containsKey("scene4_with_maxvideo_but_value_set_in_config"), "Missing key in SceneValues")
        assertEquals("0:40", panel.sceneInputs["scene4_with_maxvideo_but_value_set_in_config"]!!.sceneInput.text)
        assertEquals(2, panel.sceneLabels.size)

        // When
        val sources2s1 = TSource()
        sources2s1.file = TVideoFile(duration = 20)
        val sources2 = ArrayList<TSource>()
        sources2.add(sources2s1)
        val scene2 = TScene("scene2_with_maxvideo", timeLimit = null)
        scene2.sources = sources2

        val sources4s1 = TSource()
        sources4s1.file = TVideoFile(duration = 14)
        val sources4 = ArrayList<TSource>()
        sources4.add(sources4s1)
        val scene4 = TScene("scene4_with_maxvideo_but_value_set_in_config")
        scene4.sources = sources4

        OBSState.scenes.add(TScene("scene1_value_set_in_config"))
        OBSState.scenes.add(scene2)
        OBSState.scenes.add(TScene("scene3"))
        OBSState.scenes.add(scene4)

        panel.refreshScenes()

        assertEquals(4, panel.sceneInputs.size)
        assertEquals("0:10", panel.sceneInputs["scene1_value_set_in_config"]!!.sceneInput.text)
        assertEquals("0:20", panel.sceneInputs["scene2_with_maxvideo"]!!.sceneInput.text)
        assertEquals("0:00", panel.sceneInputs["scene3"]!!.sceneInput.text)
        assertEquals("0:40", panel.sceneInputs["scene4_with_maxvideo_but_value_set_in_config"]!!.sceneInput.text)
        assertEquals(4, panel.sceneLabels.size)
    }

    @Test
    fun testScenesAreCorrectlyOrdered() {
        val panel = SceneTablePanel()
        OBSState.scenes.add(TScene("scene1"))
        OBSState.scenes.add(TScene("scene4"))
        OBSState.scenes.add(TScene("scene2"))
        OBSState.scenes.add(TScene("scene3"))

        panel.refreshScenes()

        assertEquals(1 + 4, panel.container.componentCount)
        assertEquals("Scene", ((panel.container.components[0] as JPanel).components[0] as JLabel).text)
        assertEquals("scene1", ((panel.container.components[1] as JPanel).components[0] as JLabel).text)
        assertEquals("scene4", ((panel.container.components[2] as JPanel).components[0] as JLabel).text)
        assertEquals("scene2", ((panel.container.components[3] as JPanel).components[0] as JLabel).text)
        assertEquals("scene3", ((panel.container.components[4] as JPanel).components[0] as JLabel).text)
    }

    @Test
    fun testInputChangePassesThroughToSceneInputsAndConfig() {
        val scene1 = TScene("scene1")
        OBSState.scenes.add(scene1)
        val panel = SceneTablePanel()

        assertEquals("0:00", panel.sceneInputs["scene1"]!!.sceneInput.text)
        assertEquals(0, Config.sceneProperties.tScenes.size)

        // When
        panel.sceneInputs["scene1"]!!.sceneInput.text = "0:10"
        Config.save()

        assertEquals("0:10", panel.sceneInputs["scene1"]!!.sceneInput.text)
        assertEquals(10, scene1.timeLimit)
        assertEquals("scene1", Config.sceneProperties.tScenes[0].name)
        assertEquals(10, Config.sceneProperties.tScenes[0].timeLimit)
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testInputChangeIsHandledForActiveScene() {
        val scene2 = TScene("scene2")
        OBSState.scenes.add(TScene("scene1"))
        OBSState.scenes.add(scene2)
        OBSState.currentScene = scene2
        val panel = SceneTablePanel()
        panel.switchedScenes()

        assertEquals("0:00", panel.sceneInputs["scene2"]!!.sceneInput.text)
        assertEquals(0, Config.sceneProperties.tScenes.size)
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())

        // When
        panel.sceneInputs["scene2"]!!.sceneInput.text = "0:10"

        assertEquals("0:10", panel.sceneInputs["scene2"]!!.sceneInput.text)
        assertEquals(10, scene2.timeLimit)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testSceneTimeLimitIsSetOnRefreshScenes() {
        val scene1 = TScene("scene1", timeLimit = 10)
        OBSState.scenes.add(scene1)
        OBSState.scenes.add(TScene("scene2"))
        OBSState.currentScene = scene1

        val panel = SceneTablePanel()
        panel.refreshScenes()

        assertEquals("0:10", panel.sceneInputs["scene1"]!!.sceneInput.text)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testSceneTimeLimitIsSetAfterLoadingSceneTableWithNewConfigValues() {
        val scene1 = TScene("scene1")
        OBSState.scenes.add(scene1)
        OBSState.scenes.add(TScene("scene2"))
        val panel = SceneTablePanel()

        assertNotEquals("0:10", panel.sceneInputs["scene1"]!!.sceneInput.text)
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())

        scene1.timeLimit = 10
        OBSState.currentScene = scene1
        panel.switchedScenes()

        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testSceneInputDocumentListener() {
        val scene = TScene("scene1")
        val input = SceneInput(scene)

        // when
        input.text = "0:10"

        assertEquals("0:10", input.text)
        assertEquals(10, scene.timeLimit)

        // when
        input.text = "0:20"

        assertEquals("0:20", input.text)
        assertEquals(20, scene.timeLimit)
    }

    @Test
    fun testSceneInputDocumentListenerWithNegativeValue() {
        val scene = TScene("scene1")
        val input = SceneInput(scene)
        val focusEvent = FocusEvent(input, 0)

        // when
        input.text = "-1"
        input.focusListeners.find { it is SceneInputFocusAdapter }?.focusLost(focusEvent)

        assertEquals("0:00", input.text)
        assertEquals(null, scene.timeLimit)

        // when (using as normal)
        input.text = "0:10"

        assertEquals("0:10", input.text)
        assertEquals(10, scene.timeLimit)
    }

    @Test
    fun testSceneInputDocumentListenerChangesLimitAndCurrentMaxTimerTimeOnSpinnerChange() {
        val scene = TScene("scene1")
        val input = SceneInput(scene)
        OBSState.currentScene = scene

        // when
        input.text = "0:10"

        assertEquals("0:10", input.text)
        assertEquals(10, scene.timeLimit)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())

        // when
        OBSState.currentScene = TScene("scene2")
        input.text = "0:20"

        assertEquals("0:20", input.text)
        assertEquals(20, scene.timeLimit)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }
}