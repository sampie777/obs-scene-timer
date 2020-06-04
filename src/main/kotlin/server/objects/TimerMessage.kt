package server.objects

import com.google.gson.Gson
import objects.TimerState
import java.time.Instant
import java.time.format.DateTimeFormatter


data class TimerMessage(
    val sceneName: String,
    val elapsedTime: String,
    val timerPhase: TimerState,
    val isTimed: Boolean = false,
    val remainingTime: String = "",
    val timestamp: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
    val messageType: String = "TimerMessage"
) {

    fun json(): String {
        return Gson().toJson(this)
    }
}