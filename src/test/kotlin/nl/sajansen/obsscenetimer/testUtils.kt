package nl.sajansen.obsscenetimer

import nl.sajansen.obsscenetimer.config.Config

fun resetConfig() {
    Config.obsAddress = "ws://localhost:4444"
    Config.sceneProperties.tScenes.clear()
    Config.remoteSyncClientEnabled = false
    Config.mainWindowsIsMaximized = false
    Config.mainWindowAlwaysOnTop = false
    Config.enableApplicationLoggingToFile = false
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