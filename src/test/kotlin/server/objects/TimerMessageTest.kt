package server.objects

import objects.TimerState
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TimerMessageTest {

    @Test
    fun testDefaultConstructor() {
        val message = TimerMessage(
            "scenename",
            "00:00:10",
            TimerState.NEUTRAL
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
            "scenename",
            "00:00:10",
            TimerState.NEUTRAL,
            true,
            "00:00:20",
            "2020-06-04T16:49:58.670Z"
        )

        val json = message.json()

        assertEquals(
            "{\"sceneName\":\"scenename\",\"elapsedTime\":\"00:00:10\",\"timerState\":\"NEUTRAL\",\"isTimed\":true,\"remainingTime\":\"00:00:20\",\"timestamp\":\"2020-06-04T16:49:58.670Z\",\"messageType\":\"TimerMessage\"}",
            json
        )
    }
}