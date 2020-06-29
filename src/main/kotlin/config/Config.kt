package config

import objects.notifications.Notifications
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.util.logging.Logger

object Config {
    private val logger = Logger.getLogger(Config.toString())

    // OBS Connection
    var obsAddress: String = "ws://localhost:4444"
    var obsPassword: String = ""
    var obsReconnectionTimeout: Long = 3000
    var timerStartDelay: Long = -300
    var autoCalculateSceneLimitsBySources: Boolean = true

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

    var sceneLimitValues: HashMap<String, Int> = HashMap()

    // Logging
    var enableSceneTimestampLogger: Boolean = false
    var enableApplicationLoggingToFile: Boolean = false
    var maxLogFileSize: Int = 768 * 1024    // 750 kB

    // Window Layout
    var theme: String = "LightTheme"
    var windowRestoreLastPosition: Boolean = true
    var mainWindowLocation: Point = Point(0, 0)
    var mainWindowSize: Dimension = Dimension(900, 600)
    var mainWindowsIsMaximized: Boolean = false
    var mainWindowsIsFullscreen: Boolean = false
    var mainPanelDividerLocation: Int = 370

    // Remote Sync
    var remoteSyncServerEnabled: Boolean = false
    var remoteSyncServerPort: Int = 4050
    var remoteSyncClientEnabled: Boolean = false
    var remoteSyncClientAddress: String = obsAddress.replace(":4444", ":4050")
    var remoteSyncClientReconnectionTimeout: Long = 3000

    fun load() {
        try {
            PropertyLoader.load()
            PropertyLoader.loadConfig(this::class.java)
        } catch (e: Exception) {
            logger.severe("Failed to load Config")
            e.printStackTrace()
            Notifications.add("Failed to load configuration from file", "Configuration")
        }
    }

    fun save() {
        try {
            if (PropertyLoader.saveConfig(this::class.java)) {
                PropertyLoader.save()
            }
        } catch (e: Exception) {
            logger.severe("Failed to save Config")
            e.printStackTrace()
            Notifications.add("Failed to save configuration to file", "Configuration")
        }
    }

    fun get(key: String): Any? {
        try {
            return javaClass.getDeclaredField(key).get(this)
        } catch (e: Exception) {
            logger.severe("Could not get config key $key")
            e.printStackTrace()
            Notifications.add("Could not get configuration setting: $key", "Configuration")
        }
        return null
    }

    fun set(key: String, value: Any?) {
        try {
            javaClass.getDeclaredField(key).set(this, value)
        } catch (e: Exception) {
            logger.severe("Could not set config key $key")
            e.printStackTrace()
            Notifications.add("Could not set configuration setting: $key", "Configuration")
        }
    }

    fun enableWriteToFile(value: Boolean) {
        PropertyLoader.writeToFile = value
    }
}