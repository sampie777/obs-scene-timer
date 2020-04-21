package gui

import config.Config
import objects.Globals
import objects.OBSSceneTimer
import objects.TScene
import objects.TSource
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.test.*

class SceneTablePanelTest {

    @BeforeTest
    fun before() {
        Config.sceneLimitValues.clear()
        Globals.scenes.clear()
    }

    @Test
    fun testCreatingSceneValuesHashMapWithEmptyConfig() {
        val panel = SceneTablePanel()

        assertEquals(0, panel.sceneValues.size)
        assertEquals(0, panel.sceneLabels.size)

        // When
        Globals.scenes.add(TScene("scene1"))
        Globals.scenes.add(TScene("scene2"))

        panel.refreshScenes()

        assertEquals(2, panel.sceneValues.size)
        assertEquals(2, panel.sceneLabels.size)
    }

    @Test
    fun testCreatingSceneValuesHashMapWithFilledConfig() {
        Config.sceneLimitValues["scene1"] = 10
        val panel = SceneTablePanel()

        assertEquals(1, panel.sceneValues.size)
        assertTrue(panel.sceneValues.containsKey("scene1"), "SceneValues doesn't contain key 'scene1'")
        assertEquals(10, panel.sceneValues["scene1"])
        assertEquals(0, panel.sceneLabels.size)

        // When
        Globals.scenes.add(TScene("scene1"))
        Globals.scenes.add(TScene("scene2"))

        panel.refreshScenes()

        assertEquals(2, panel.sceneValues.size)
        assertEquals(2, panel.sceneLabels.size)
    }

    @Test
    fun testCreatingCorrectSceneValuesHashMapWithFilledConfigAndMaxVideoSizeAvailable() {
        Config.sceneLimitValues["scene1_value_set_in_config"] = 10
        Config.sceneLimitValues["scene4_with_maxvideo_but_value_set_in_config"] = 40
        val panel = SceneTablePanel()

        assertEquals(2, panel.sceneValues.size)
        assertTrue(panel.sceneValues.containsKey("scene1_value_set_in_config"), "Missing key in SceneValues")
        assertEquals(10, panel.sceneValues["scene1_value_set_in_config"])
        assertTrue(panel.sceneValues.containsKey("scene4_with_maxvideo_but_value_set_in_config"), "Missing key in SceneValues")
        assertEquals(40, panel.sceneValues["scene4_with_maxvideo_but_value_set_in_config"])
        assertEquals(0, panel.sceneLabels.size)

        // When
        val sources2s1 = TSource()
        sources2s1.videoLength = 20
        val sources2 = ArrayList<TSource>()
        sources2.add(sources2s1)
        val scene2 = TScene("scene2_with_maxvideo")
        scene2.sources = sources2

        val sources4s1 = TSource()
        sources4s1.videoLength = 14
        val sources4 = ArrayList<TSource>()
        sources4.add(sources4s1)
        val scene4 = TScene("scene4_with_maxvideo_but_value_set_in_config")
        scene4.sources = sources4

        Globals.scenes.add(TScene("scene1_value_set_in_config"))
        Globals.scenes.add(scene2)
        Globals.scenes.add(TScene("scene3"))
        Globals.scenes.add(scene4)

        panel.refreshScenes()

        assertEquals(4, panel.sceneValues.size)
        assertEquals(10, panel.sceneValues["scene1_value_set_in_config"])
        assertEquals(20, panel.sceneValues["scene2_with_maxvideo"])
        assertEquals(0, panel.sceneValues["scene3"])
        assertEquals(40, panel.sceneValues["scene4_with_maxvideo_but_value_set_in_config"])
        assertEquals(4, panel.sceneLabels.size)
    }

    @Test
    fun testScenesAreCorrectlyOrdered() {
        val panel = SceneTablePanel()
        Globals.scenes.add(TScene("scene1"))
        Globals.scenes.add(TScene("scene4"))
        Globals.scenes.add(TScene("scene2"))
        Globals.scenes.add(TScene("scene3"))

        panel.refreshScenes()

        assertEquals(1 + 4, panel.container.componentCount)
        assertEquals("Scene", ((panel.container.components[0] as JPanel).components[0] as JLabel).text)
        assertEquals("scene1", ((panel.container.components[1] as JPanel).components[0] as JLabel).text)
        assertEquals("scene4", ((panel.container.components[2] as JPanel).components[0] as JLabel).text)
        assertEquals("scene2", ((panel.container.components[3] as JPanel).components[0] as JLabel).text)
        assertEquals("scene3", ((panel.container.components[4] as JPanel).components[0] as JLabel).text)
    }

    @Test
    fun testInputChangePassesThroughToSceneValuesAndConfig() {
        Globals.scenes.add(TScene("scene1"))
        val panel = SceneTablePanel()

        assertEquals(0, panel.sceneInputs["scene1"]!!.value)
        assertEquals(0, panel.sceneValues["scene1"])
        assertNull(Config.sceneLimitValues["scene1"])

        // When
        panel.sceneInputs["scene1"]!!.value = 10

        assertEquals(10, panel.sceneInputs["scene1"]!!.value)
        assertEquals(10, panel.sceneValues["scene1"])
        assertEquals(10, Config.sceneLimitValues["scene1"])
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testInputChangeIsHandledForActiveScene() {
        Globals.scenes.add(TScene("scene1"))
        Globals.scenes.add(TScene("scene2"))
        OBSSceneTimer.setCurrentSceneName("scene2")
        val panel = SceneTablePanel()
        panel.switchedScenes()

        assertEquals(0, panel.sceneInputs["scene2"]!!.value)
        assertEquals(0, panel.sceneValues["scene2"])
        assertNull(Config.sceneLimitValues["scene2"])
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())

        // When
        panel.sceneInputs["scene2"]!!.value = 10

        assertEquals(10, panel.sceneInputs["scene2"]!!.value)
        assertEquals(10, panel.sceneValues["scene2"])
        assertEquals(10, Config.sceneLimitValues["scene2"])
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }
}