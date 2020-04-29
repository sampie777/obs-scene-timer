package objects

import kotlin.test.*

class OBSSceneTimerTest {

    @BeforeTest
    fun before() {
        OBSSceneTimer.stop()
    }

    @Test
    fun testResetTimerValue() {
        // Given
        OBSSceneTimer.increase()
        OBSSceneTimer.increase()

        assertNotEquals(0, OBSSceneTimer.getValue())

        // When
        OBSSceneTimer.resetValue()

        // Then
        assertEquals(0, OBSSceneTimer.getValue())
    }

    @Test
    fun testIncreaseTimer() {
        // Given
        val valueBefore = OBSSceneTimer.getValue()

        // When
        OBSSceneTimer.increase()

        // Then
        assertEquals(valueBefore + 1, OBSSceneTimer.getValue())
    }

    @Test
    fun setAndGetMaxTimerValue() {
        val maxTimerValue = 100L

        OBSSceneTimer.setMaxTimerValue(0)

        assertNotEquals(maxTimerValue, OBSSceneTimer.getMaxTimerValue())

        OBSSceneTimer.setMaxTimerValue(maxTimerValue)

        assertEquals(maxTimerValue, OBSSceneTimer.getMaxTimerValue())
    }

    @Test
    fun testResetTimerWhenRunning() {
        OBSSceneTimer.reset()
        OBSSceneTimer.increase()
        assertEquals(1, OBSSceneTimer.getValue())

        OBSSceneTimer.reset()
        assertEquals(0, OBSSceneTimer.getValue())
    }

    @Test
    fun testStopTimerWhenRunning() {
        OBSSceneTimer.reset()

        assertTrue(OBSSceneTimer.stop())
    }
}