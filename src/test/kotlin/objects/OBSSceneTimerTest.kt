package objects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OBSSceneTimerTest {

    @Test
    fun testResetTimer() {
        // Given
        OBSSceneTimer.increaseTimer()
        OBSSceneTimer.increaseTimer()

        assertNotEquals(0, OBSSceneTimer.getTimerValue())

        // When
        OBSSceneTimer.resetTimer()

        // Then
        assertEquals(0, OBSSceneTimer.getTimerValue())
    }

    @Test
    fun testIncreaseTimer() {
        // Given
        val valueBefore = OBSSceneTimer.getTimerValue()

        // When
        OBSSceneTimer.increaseTimer()

        // Then
        assertEquals(valueBefore + 1, OBSSceneTimer.getTimerValue())
    }

    @Test
    fun setAndGetCurrentSceneName() {
        val sceneName = "mySceneName"

        OBSSceneTimer.setCurrentSceneName("nothing")

        assertNotEquals(sceneName, OBSSceneTimer.getCurrentSceneName())

        OBSSceneTimer.setCurrentSceneName(sceneName)

        assertEquals(sceneName, OBSSceneTimer.getCurrentSceneName())
    }

    @Test
    fun setAndGetMaxTimerValue() {
        val maxTimerValue = 100L

        OBSSceneTimer.setMaxTimerValue(0)

        assertNotEquals(maxTimerValue, OBSSceneTimer.getMaxTimerValue())

        OBSSceneTimer.setMaxTimerValue(maxTimerValue)

        assertEquals(maxTimerValue, OBSSceneTimer.getMaxTimerValue())
    }
}