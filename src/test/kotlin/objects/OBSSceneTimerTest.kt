package objects

import config.Config
import objects.notifications.Notifications
import themes.Theme
import kotlin.test.*

class OBSSceneTimerTest {

    @BeforeTest
    fun before() {
        Config.remoteSyncClientEnabled = false
        Notifications.clear()
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

    @Test
    fun testCantResetTimerWhenInRemoteSyncClientMode() {
        Config.remoteSyncClientEnabled = true
        OBSSceneTimer.reset()

        assertEquals(1, Notifications.unreadNotifications)
        assertEquals("Can't restart Timer while in client mode", Notifications.list[0].message)
    }

    @Test
    fun testGetTimerState() {
        Config.remoteSyncClientEnabled = false
        Config.smallMinLimitForLimitApproaching = 0
        Config.smallTimeDifferenceForLimitApproaching = 1
        Config.largeMinLimitForLimitApproaching = 100
        OBSSceneTimer.setMaxTimerValue(3)

        OBSSceneTimer.increase()   // 1
        assertEquals(TimerState.NEUTRAL, OBSSceneTimer.getTimerState())

        OBSSceneTimer.increase()   // 2
        assertEquals(TimerState.APPROACHING, OBSSceneTimer.getTimerState())

        OBSSceneTimer.increase()   // 3
        assertEquals(TimerState.EXCEEDED, OBSSceneTimer.getTimerState())
    }
}