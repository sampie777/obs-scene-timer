package gui

import config.Config
import objects.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.event.ChangeEvent
import kotlin.test.*

class SceneTablePanelTest {

    @BeforeTest
    fun before() {
        Config.sceneProperties.tScenes.clear()
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
        assertEquals(10, panel.sceneInputs["scene1"]!!.value)
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
        assertEquals(10, panel.sceneInputs["scene1_value_set_in_config"]!!.value)
        assertTrue(panel.sceneInputs.containsKey("scene4_with_maxvideo_but_value_set_in_config"), "Missing key in SceneValues")
        assertEquals(40, panel.sceneInputs["scene4_with_maxvideo_but_value_set_in_config"]!!.value)
        assertEquals(2, panel.sceneLabels.size)

        // When
        val sources2s1 = TSource()
        sources2s1.videoLength = 20
        val sources2 = ArrayList<TSource>()
        sources2.add(sources2s1)
        val scene2 = TScene("scene2_with_maxvideo", timeLimit = null)
        scene2.sources = sources2

        val sources4s1 = TSource()
        sources4s1.videoLength = 14
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
        assertEquals(10, panel.sceneInputs["scene1_value_set_in_config"]!!.value)
        assertEquals(20, panel.sceneInputs["scene2_with_maxvideo"]!!.value)
        assertEquals(0, panel.sceneInputs["scene3"]!!.value)
        assertEquals(40, panel.sceneInputs["scene4_with_maxvideo_but_value_set_in_config"]!!.value)
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

        assertEquals(0, panel.sceneInputs["scene1"]!!.value)
        assertEquals(0, Config.sceneProperties.tScenes.size)

        // When
        panel.sceneInputs["scene1"]!!.value = 10
        Config.save()

        assertEquals(10, panel.sceneInputs["scene1"]!!.value)
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

        assertEquals(0, panel.sceneInputs["scene2"]!!.value)
        assertEquals(0, Config.sceneProperties.tScenes.size)
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())

        // When
        panel.sceneInputs["scene2"]!!.value = 10

        assertEquals(10, panel.sceneInputs["scene2"]!!.value)
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

        assertEquals(10, panel.sceneInputs["scene1"]!!.value)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testSceneTimeLimitIsSetAfterLoadingSceneTableWithNewConfigValues() {
        val scene1 = TScene("scene1")
        OBSState.scenes.add(scene1)
        OBSState.scenes.add(TScene("scene2"))
        val panel = SceneTablePanel()

        assertNotEquals(10, panel.sceneInputs["scene1"]!!.value)
        assertEquals(0, OBSSceneTimer.getMaxTimerValue())

        scene1.timeLimit = 10
        OBSState.currentScene = scene1
        panel.switchedScenes()

        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testSceneInputChangeListenerChangesLimitOnSpinnerChange() {
        val scene = TScene("scene1")
        val source = JSpinner()
        val listener = SceneInputChangeListener(scene)
        val event = ChangeEvent(source)

        // when
        source.value = 10
        listener.stateChanged(event)

        assertEquals(10, source.value)
        assertEquals(10, scene.timeLimit)
        assertEquals("0:00:10", source.toolTipText)

        // when
        source.value = 20
        listener.stateChanged(event)

        assertEquals(20, source.value)
        assertEquals(20, scene.timeLimit)
        assertEquals("0:00:20", source.toolTipText)
    }

    @Test
    fun testSceneInputChangeListenerResetsLimitOnNegativeSpinnerValueAndCatchesDebounceEffect() {
        val scene = TScene("scene1", timeLimit = 10)
        val listener = SceneInputChangeListener(scene)
        val source = JSpinner()
        source.addChangeListener(listener)

        // when
        source.value = -1

        assertEquals(0, source.value)
        assertEquals(null, scene.timeLimit)
        assertEquals("0:00:00", source.toolTipText)

        // when (using as normal)
        source.value = 10

        assertEquals(10, source.value)
        assertEquals(10, scene.timeLimit)
        assertEquals("0:00:10", source.toolTipText)
    }

    @Test
    fun testSceneInputChangeListenerChangesLimitAndCurrentMaxTimerTimeOnSpinnerChange() {
        val scene = TScene("scene1")
        val source = JSpinner()
        val listener = SceneInputChangeListener(scene)
        val event = ChangeEvent(source)
        OBSState.currentScene = scene

        // when
        source.value = 10
        listener.stateChanged(event)

        assertEquals(10, source.value)
        assertEquals(10, scene.timeLimit)
        assertEquals("0:00:10", source.toolTipText)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())

        // when
        OBSState.currentScene = TScene("scene2")
        source.value = 20
        listener.stateChanged(event)

        assertEquals(20, source.value)
        assertEquals(20, scene.timeLimit)
        assertEquals("0:00:20", source.toolTipText)
        assertEquals(10, OBSSceneTimer.getMaxTimerValue())
    }
}