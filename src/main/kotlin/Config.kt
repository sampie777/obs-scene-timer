import java.awt.Color

object Config {
    val obsAddress: String = "ws://localhost:4444"
    val obsConnectionDelay: Long = 1000

    val timerBackgroundColor: Color = Color.LIGHT_GRAY
    val approachingLimitColor: Color = Color.ORANGE
    val exceededLimitColor: Color = Color.RED

    const val largeMinLimitForLimitApproaching: Long = 60
    const val largeTimeDifferenceForLimitApproaching: Long = 30
    const val smallMinLimitForLimitApproaching: Long = 20
    const val smallTimeDifferenceForLimitApproaching: Long = 10
}