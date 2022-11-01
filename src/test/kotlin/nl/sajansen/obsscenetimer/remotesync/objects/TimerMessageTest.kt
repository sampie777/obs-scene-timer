package nl.sajansen.obsscenetimer.remotesync.objects

import nl.sajansen.obsscenetimer.objects.TimerState
import nl.sajansen.obsscenetimer.resetConfig
import org.junit.Before
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TimerMessageTest {

    @Before
    fun before() {
        resetConfig()
    }

    @Test
    fun testDefaultConstructor() {
        val message = TimerMessage(
            sceneName = "scenename",
            elapsedTime = "00:00:10",
            elapsedTimeRaw = 10L,
            timerState = TimerState.NEUTRAL
        )
        val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val strippedNow = now.substring(0, now.length - 3)

        assertFalse(message.isTimed)
        assertEquals("", message.remainingTime)
        assertTrue(message.timestamp.contains(strippedNow))
    }

    @Test
    fun testToJson() {
        val message = TimerMessage(
            sceneName = "scenename",
            elapsedTime = "00:00:10",
            elapsedTimeRaw = 10L,
            timerState = TimerState.NEUTRAL,
            isTimed = true,
            remainingTime = "00:00:20",
            remainingTimeRaw = 20L,
            maximumTime = 30L,
            timestamp = "2020-06-04T16:49:58.670Z"
        )

        val json = message.json()

        assertEquals(
            "{\"sceneName\":\"scenename\",\"elapsedTime\":\"00:00:10\",\"elapsedTimeRaw\":10,\"timerState\":\"NEUTRAL\",\"isTimed\":true,\"remainingTime\":\"00:00:20\",\"remainingTimeRaw\":20,\"maximumTime\":30,\"timestamp\":\"2020-06-04T16:49:58.670Z\",\"messageType\":\"TimerMessage\"}",
            json
        )
    }
}