package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.config.Config
import java.awt.EventQueue

fun resetConfig() {
    Config.obsAddress = "ws://localhost:4444"
    Config.obsHost = "localhost"
    Config.obsPort = 4455
    Config.sceneProperties.tScenes.clear()
    Config.remoteSyncClientEnabled = false
    Config.mainWindowsIsMaximized = false
    Config.mainWindowAlwaysOnTop = false
    Config.remoteSyncServerEnabled = false
    Config.remoteSyncClientEnabled = false
    Config.autoCalculateSceneLimitsBySources = true
    Config.largeMinLimitForLimitApproaching = 60
    Config.smallMinLimitForLimitApproaching = 20
    Config.smallTimeDifferenceForLimitApproaching = 10
    Config.timerFlashForRemainingTimeLessThan = 10
    Config.timerCountUpFontSize = 80
    Config.timerCountDownFontSize = 100
}

fun waitForSwing(amount: Int = 2){
    if (amount <= 0) {
        return
    }

    if (EventQueue.isDispatchThread()) {
        return
    }

    try {
        EventQueue.invokeAndWait {  }
    } catch (_: Throwable) {
    }

    waitForSwing(amount - 1)
}