package config

import objects.notifications.Notifications
import java.awt.Color
import java.util.logging.Logger

object Config {
    private val logger = Logger.getLogger(Config.toString())

    var obsAddress: String = "ws://localhost:4444"
    var obsPassword: String = ""
    var obsReconnectionTimeout: Long = 3000

    var timerBackgroundColor: Color = Color(230,230,230)
    var approachingLimitColor: Color = Color.ORANGE
    var exceededLimitColor: Color = Color.RED
    var timerCountUpFontSize: Int = 80
    var timerCountDownFontSize: Int = 100

    var largeMinLimitForLimitApproaching: Long = 60
    var largeTimeDifferenceForLimitApproaching: Long = 30
    var smallMinLimitForLimitApproaching: Long = 20
    var smallTimeDifferenceForLimitApproaching: Long = 10

    var sceneLimitValues: HashMap<String, Int> = HashMap()

    var enableSceneTimestampLogger: Boolean = false

    fun load() {
        try {
            PropertyLoader.load()
            PropertyLoader.loadConfig(this::class.java)
        } catch (e: Error) {
            logger.severe("Failed to load Config")
            e.printStackTrace()
            Notifications.add("Failed to load configuration from file", "Configuration")
        }
    }

    fun save() {
        try {
            PropertyLoader.saveConfig(this::class.java)
            PropertyLoader.save()
        } catch (e: Error) {
            logger.severe("Failed to save Config")
            e.printStackTrace()
            Notifications.add("Failed to save configuration to file", "Configuration")
        }
    }

    fun get(key: String): Any? {
        try {
            return javaClass.getDeclaredField(key).get(this)
        } catch (t: Throwable) {
            logger.severe("Could not get config key $key")
            t.printStackTrace()
            Notifications.add("Could not get configuration setting: $key", "Configuration")
        }
        return null
    }

    fun set(key: String, value: Any?) {
        try {
            javaClass.getDeclaredField(key).set(this, value)
        } catch (t: Throwable) {
            logger.severe("Could not set config key $key")
            t.printStackTrace()
            Notifications.add("Could not set configuration setting: $key", "Configuration")
        }
    }
}