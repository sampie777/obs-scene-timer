package nl.sajansen.obsscenetimer.remotesync.objects

import com.google.gson.Gson
import nl.sajansen.obsscenetimer.objects.TimerState
import java.time.Instant
import java.time.format.DateTimeFormatter


data class TimerMessage(
    val sceneName: String,
    val elapsedTime: String,
    val elapsedTimeRaw: Long,
    val timerState: TimerState,
    val isTimed: Boolean = false,
    val remainingTime: String = "",
    val remainingTimeRaw: Long = -1,
    val maximumTime: Long = -1,
    val timestamp: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    val messageType: String = "TimerMessage"
) {

    fun json(): String {
        return Gson().toJson(this)
    }
}