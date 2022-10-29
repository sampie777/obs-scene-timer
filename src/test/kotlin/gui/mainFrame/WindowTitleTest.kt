package gui.mainFrame

import config.Config
import objects.OBSSceneTimer
import objects.TScene
import obs.OBSState
import org.junit.Before
import resetConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowTitleTest {
    @Before
    fun before() {
        resetConfig()
    }

    @Test
    fun testTitleWithoutVariables() {
        Config.mainWindowTitle = "Title without variables"
        assertEquals("Title without variables", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithSceneVariable() {
        Config.mainWindowTitle = "Title scene variable: {${WindowTitle.SCENE_NAME}}"
        OBSState.currentScene = TScene("Scene 1")
        assertEquals("Title scene variable: Scene 1", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithFaultySceneVariable() {
        Config.mainWindowTitle = "Title scene variable: ${WindowTitle.SCENE_NAME}}"
        OBSState.currentScene = TScene("Scene 1")
        assertEquals("Title scene variable: ${WindowTitle.SCENE_NAME}}", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithFaultySceneVariable2() {
        Config.mainWindowTitle = "Title scene variable: {xx}"
        OBSState.currentScene = TScene("Scene 1")
        assertEquals("Title scene variable: {xx}", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithTimerVariable() {
        Config.mainWindowTitle = "Title timer variable: {${WindowTitle.TIMER_ELAPSED}}"
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()

        assertEquals(1, OBSSceneTimer.getValue())
        assertEquals("Title timer variable: 0:00:01", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithTimerRemainingVariableWithNoTimeLimit() {
        Config.mainWindowTitle = "Title timer variable: {${WindowTitle.TIMER_REMAINING}}"
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()
        OBSSceneTimer.setMaxTimerValue(0)

        assertEquals(1, OBSSceneTimer.getValue())
        assertEquals("Title timer variable: ", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithTimerRemainingVariableBeforeTimeLimit() {
        Config.mainWindowTitle = "Title timer variable: {${WindowTitle.TIMER_REMAINING}}"
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()
        OBSSceneTimer.setMaxTimerValue(3)

        assertEquals(1, OBSSceneTimer.getValue())
        assertEquals("Title timer variable: 0:00:02", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithTimerRemainingVariableOnAndAfterTimeLimit() {
        Config.mainWindowTitle = "Title timer variable: {${WindowTitle.TIMER_REMAINING}}"
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()
        OBSSceneTimer.increase()
        OBSSceneTimer.increase()
        OBSSceneTimer.setMaxTimerValue(3)

        assertEquals(3, OBSSceneTimer.getValue())
        assertEquals("Title timer variable: 0:00:00", WindowTitle.generateWindowTitle())

        OBSSceneTimer.increase()
        assertEquals(4, OBSSceneTimer.getValue())
        assertEquals("Title timer variable: -0:00:01", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithMultipleVariables() {
        Config.mainWindowTitle = "Title variables: {${WindowTitle.SCENE_NAME}} - {${WindowTitle.TIMER_ELAPSED}}"
        OBSState.currentScene = TScene("Scene 1")
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()

        assertEquals(1, OBSSceneTimer.getValue())
        assertEquals("Title variables: Scene 1 - 0:00:01", WindowTitle.generateWindowTitle())
    }

    @Test
    fun testTitleWithDuplicateVariables() {
        Config.mainWindowTitle = "Title variables: {${WindowTitle.SCENE_NAME}} - {${WindowTitle.SCENE_NAME}} - {${WindowTitle.TIMER_ELAPSED}}"
        OBSState.currentScene = TScene("Scene 1")
        OBSSceneTimer.stop()
        OBSSceneTimer.resetValue()
        OBSSceneTimer.increase()

        assertEquals(1, OBSSceneTimer.getValue())
        assertEquals("Title variables: Scene 1 - Scene 1 - 0:00:01", WindowTitle.generateWindowTitle())
    }
}