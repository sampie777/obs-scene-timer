package nl.sajansen.obsscenetimer.objects

import getTimeAsClock
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.remotesync.objects.TimerMessage
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.max

class SceneTimerTask : TimerTask() {
    override fun run() {
        OBSSceneTimer.increase()
        GUI.refreshTimer()

        Config.save()
    }
}

object OBSSceneTimer {
    private val logger = LoggerFactory.getLogger(OBSSceneTimer::class.java.name)

    private var timerValue = 0L
    private var maxTimerValue = 0L

    private var timer = Timer()
    private const val timerIntervalSecond = 1000L

    var timerMessage: TimerMessage? = null

    fun reset() {
        logger.info("Resetting timer")
        if (Config.remoteSyncClientEnabled) {
            logger.warn("Can't reset Scene Timer because Client mode is enabled")
            Notifications.add("Can't restart Timer while in client mode", "Timer")
            return
        }

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
            logger.error("Error occurred during canceling timer: ${e.localizedMessage}")
            e.printStackTrace()
        }
        return false
    }

    fun increase() {
        timerValue++
    }

    fun getValue(): Long {
        if (Config.remoteSyncClientEnabled) {
            return timerMessage?.elapsedTimeRaw ?: 0
        }
        return timerValue
    }

    fun getRemainingTime(): Long {
        if (Config.remoteSyncClientEnabled) {
            return timerMessage?.remainingTimeRaw ?: 0
        }
        return getMaxTimerValue() - getValue()
    }

    fun getTimerAsClock(): String {
        return getTimeAsClock(getValue())
    }

    fun setMaxTimerValue(value: Long) {
        maxTimerValue = value
    }

    fun getMaxTimerValue(): Long {
        if (Config.remoteSyncClientEnabled) {
            return timerMessage?.maximumTime ?: 0
        }
        return maxTimerValue
    }

    fun getTimerState(): TimerState {
        if (Config.remoteSyncClientEnabled) {
            return timerMessage?.timerState ?: TimerState.NEUTRAL
        }

        if (getMaxTimerValue() == 0L) {
            return TimerState.NEUTRAL
        }

        if (getValue() >= getMaxTimerValue()) {
            return TimerState.EXCEEDED

        } else if (getMaxTimerValue() >= Config.largeMinLimitForLimitApproaching
            && getValue() + Config.largeTimeDifferenceForLimitApproaching >= getMaxTimerValue()
        ) {
            return TimerState.APPROACHING

        } else if (getMaxTimerValue() < Config.largeMinLimitForLimitApproaching
            && getMaxTimerValue() >= Config.smallMinLimitForLimitApproaching
            && getValue() + Config.smallTimeDifferenceForLimitApproaching >= getMaxTimerValue()
        ) {
            return TimerState.APPROACHING

        } else {
            return TimerState.NEUTRAL
        }
    }
}