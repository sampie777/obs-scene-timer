package objects

import getTimeAsClock
import java.util.logging.Logger

object OBSSceneTimer {
    private val logger = Logger.getLogger(OBSSceneTimer::class.java.name)

    private var timerValue = 0L
    private var maxTimerValue = 0L

    fun resetTimer() {
        logger.info("Resetting timer to 0")
        timerValue = 0L
    }

    fun increaseTimer() {
        timerValue++
    }

    fun getTimerValue(): Long {
        return timerValue
    }

    fun getTimerAsClock(): String {
        return getTimeAsClock(getTimerValue())
    }

    fun setMaxTimerValue(value: Long) {
        maxTimerValue = value
    }

    fun getMaxTimerValue(): Long {
        return maxTimerValue
    }
}