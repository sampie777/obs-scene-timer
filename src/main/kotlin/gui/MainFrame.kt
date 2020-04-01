package gui

import objects.OBSSceneTimer
import java.util.logging.Logger
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

class MainFrame : JFrame(), Refreshable {
    val logger = Logger.getLogger("MainFrame")

    private var sceneTablePanel: SceneTablePanel? = null
    private var timerPanel: TimerPanel? = null

    init {
        GUI.register(this)
        initGUI()
    }

    fun initGUI() {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        add(mainPanel)

        sceneTablePanel = SceneTablePanel()
        timerPanel = TimerPanel()
        mainPanel.add(sceneTablePanel)
        mainPanel.add(timerPanel)

        setSize(700, 600)
        title = "Countdown Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
    }

    override fun refreshTimer() {
        title = "${OBSSceneTimer.getCurrentSceneName()}: ${OBSSceneTimer.getTimerAsClock()}"

        val sceneMaxDuration = OBSSceneTimer.getMaxTimerValue()

        if (sceneMaxDuration == 0L) {
            timerPanel?.background = Config.timerBackgroundColor
            return
        }

        if (OBSSceneTimer.getTimerValue() >= sceneMaxDuration) {
            logger.severe("Timer exceeded!")
            timerPanel?.background = Config.exceededLimitColor

        } else if (sceneMaxDuration >= Config.largeMinLimitForLimitApproaching
            && OBSSceneTimer.getTimerValue() + Config.largeTimeDifferenceForLimitApproaching >= sceneMaxDuration
        ) {
            logger.severe("Timer almost exceeded!")
            timerPanel?.background = Config.approachingLimitColor

        } else if (sceneMaxDuration < Config.largeMinLimitForLimitApproaching
            && sceneMaxDuration >= Config.smallMinLimitForLimitApproaching
            && OBSSceneTimer.getTimerValue() + Config.smallTimeDifferenceForLimitApproaching >= sceneMaxDuration
        ) {
            logger.severe("Timer almost exceeded!")
            timerPanel?.background = Config.approachingLimitColor

        } else {
            timerPanel?.background = Config.timerBackgroundColor
        }
    }
}