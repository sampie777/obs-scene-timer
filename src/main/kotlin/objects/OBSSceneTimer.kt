package objects

import GUI
import config.Config
import getTimeAsClock
import java.util.*
import java.util.logging.Logger
import kotlin.math.max

class SceneTimerTask : TimerTask() {
    override fun run() {
        OBSSceneTimer.increase()
        GUI.refreshTimer()

        Config.save()
    }
}

object OBSSceneTimer {
    private val logger = Logger.getLogger(OBSSceneTimer::class.java.name)

    private var timerValue = 0L
    private var maxTimerValue = 0L

    private var timer = Timer()
    private const val timerIntervalSecond = 1000L

    fun reset() {
        logger.info("Resetting timer")

        stop()
        resetValue()

        timer.scheduleAtFixedRate(
            SceneTimerTask(),
            max(0, timerIntervalSecond + Config.timerStartDelay),
            timerIntervalSecond
        )
    }

    fun resetValue() {
        logger.info("Resetting timer value")
        timerValue = 0L
    }

    fun stop(): Boolean {
        try {
            logger.info("Trying to cancel timer")
            timer.cancel()
            timer = Timer()
            logger.info("Timer canceled")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun increase() {
        timerValue++
    }

    fun getValue(): Long {
        return timerValue
    }

    fun getTimerAsClock(): String {
        return getTimeAsClock(getValue())
    }

    fun setMaxTimerValue(value: Long) {
        maxTimerValue = value
    }

    fun getMaxTimerValue(): Long {
        return maxTimerValue
    }
}