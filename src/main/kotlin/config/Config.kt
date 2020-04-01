package config

import java.awt.Color

object Config {
    var obsAddress: String = "ws://localhost:4444"
    var obsConnectionDelay: Long = 1000

    var timerBackgroundColor: Color = Color.LIGHT_GRAY
    var approachingLimitColor: Color = Color.ORANGE
    var exceededLimitColor: Color = Color.RED

    var largeMinLimitForLimitApproaching: Long = 60
    var largeTimeDifferenceForLimitApproaching: Long = 30
    var smallMinLimitForLimitApproaching: Long = 20
    var smallTimeDifferenceForLimitApproaching: Long = 10

    var sceneLimitValues: HashMap<String, Int> = HashMap()

    fun load() {
        PropertyLoader.load()
        PropertyLoader.loadConfig(this::class.java)
    }

    fun save() {
        PropertyLoader.saveConfig(this::class.java)
        PropertyLoader.save()
    }
}