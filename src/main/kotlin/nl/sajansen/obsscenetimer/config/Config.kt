package nl.sajansen.obsscenetimer.config

import nl.sajansen.obsscenetimer.gui.mainFrame.WindowTitle
import nl.sajansen.obsscenetimer.objects.Json
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.utils.Rollbar
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Dimension
import java.awt.Point

object Config {
    private val logger = LoggerFactory.getLogger(Config.toString())

    // OBS Connection
    var obsAddress: String = "ws://localhost:4455"  // Deprecated
    var obsHost: String = "localhost"
    var obsPort: Int = 4455
    var obsPassword: String = ""
    var obsReconnectionTimeout: Long = 3000
    var timerStartDelay: Long = -300
    var autoCalculateSceneLimitsBySources: Boolean = true
    var sumVlcPlaylistSourceLengths: Boolean = true
    var reverseSceneOrder: Boolean = false

    // Timer Style
    @Deprecated("This value won't be of any use in future releases. Please use a Theme to specify a custom color")
    var timerBackgroundColor: Color? = null

    @Deprecated("This value won't be of any use in future releases. Please use a Theme to specify a custom color")
    var approachingLimitColor: Color? = null

    @Deprecated("This value won't be of any use in future releases. Please use a Theme to specify a custom color")
    var exceededLimitColor: Color? = null

    var timerCountUpFontSize: Int = 80
    var timerCountDownFontSize: Int = 100

    var largeMinLimitForLimitApproaching: Long = 60
    var largeTimeDifferenceForLimitApproaching: Long = 30
    var smallMinLimitForLimitApproaching: Long = 20
    var smallTimeDifferenceForLimitApproaching: Long = 10
    var timerFlashForRemainingTimeLessThan: Long = 10
    var timerFlashDurationInMilliSeconds: Int = 100

    var maxGroups: Int = 32
    var sceneProperties: Json.TScenes = Json.TScenes(ArrayList())

    // Logging
    var enableSceneTimestampLogger: Boolean = false
    var enableAutomaticErrorReporting: Boolean = true

    // Window Layout
    var theme: String = "LightTheme"
    var windowRestoreLastPosition: Boolean = true
    var mainWindowLocation: Point = Point(0, 0)
    var mainWindowSize: Dimension = Dimension(900, 600)
    var mainWindowsIsMaximized: Boolean = false
    var mainWindowsIsFullscreen: Boolean = false
    var mainPanelDividerLocation: Int = 370
    var mainWindowTitle: String =
        "${WindowTitle.VARIABLE_IDENTIFIER.format(WindowTitle.SCENE_NAME)}: ${
            WindowTitle.VARIABLE_IDENTIFIER.format(
                WindowTitle.TIMER_ELAPSED
            )
        }"
    var mainWindowAlwaysOnTop: Boolean = false

    // Remote Sync
    var remoteSyncServerEnabled: Boolean = false
    var remoteSyncServerPort: Int = 4050
    var remoteSyncClientEnabled: Boolean = false
    var remoteSyncClientAddress: String = obsAddress.replace(":4444", ":4050")
    var remoteSyncClientReconnectionTimeout: Long = 3000

    // Other
    var updatesCheckForUpdates: Boolean = true

    fun load() {
        try {
            PropertyLoader.load()
            PropertyLoader.loadConfig(this::class.java)
        } catch (e: Exception) {
            logger.error("Failed to load Config. ${e.localizedMessage}")
            e.printStackTrace()
            Notifications.add("Failed to load configuration from file: ${e.localizedMessage}", "Configuration")
        }
    }

    fun save() {
        OBSState.scenes.forEach { tScene ->
            sceneProperties.tScenes.removeIf { it.name == tScene.name }
            sceneProperties.tScenes.add(tScene.toJson())
        }

        try {
            if (PropertyLoader.saveConfig(this::class.java)) {
                PropertyLoader.save()
            }
        } catch (e: Exception) {
            logger.error("Failed to save Config. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("properties" to PropertyLoader.getUserProperties().toString()), "Failed to save Config")
            e.printStackTrace()
            Notifications.add("Failed to save configuration to file: ${e.localizedMessage}", "Configuration")
        }
    }

    fun get(key: String): Any? {
        try {
            return javaClass.getDeclaredField(key).get(this)
        } catch (e: Exception) {
            logger.error("Could not get config key $key. ${e.localizedMessage}")
            e.printStackTrace()
            Notifications.add("Could not get configuration setting: $key (${e.localizedMessage})", "Configuration")
        }
        return null
    }

    fun set(key: String, value: Any?) {
        try {
            javaClass.getDeclaredField(key).set(this, value)
        } catch (e: Exception) {
            logger.error("Could not set config key $key. ${e.localizedMessage}")
            Rollbar.error(e, mapOf("key" to key, "value" to value), "Could not set config key $key")
            e.printStackTrace()
            Notifications.add("Could not set configuration setting: $key (${e.localizedMessage})", "Configuration")
        }
    }

    fun enableWriteToFile(value: Boolean) {
        PropertyLoader.writeToFile = value
    }
}