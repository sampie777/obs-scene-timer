package gui

import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.util.logging.Logger
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private var timerPanel: TimerPanel? = null

    init {
        GUI.register(this)
        initGUI()
    }

    private fun initGUI() {
        val mainPanel = JPanel()
        mainPanel.layout = BorderLayout(0, 0)
        add(mainPanel)

        val leftPanel = JPanel()
        leftPanel.layout = BorderLayout(0, 0)
        mainPanel.add(leftPanel, BorderLayout.LINE_START)

        val sceneTablePanel = SceneTablePanel()
        leftPanel.add(sceneTablePanel, BorderLayout.CENTER)

        val obsStatusPanel = OBSStatusPanel()
        leftPanel.add(obsStatusPanel, BorderLayout.PAGE_END)

        timerPanel = TimerPanel()
        mainPanel.add(timerPanel, BorderLayout.CENTER)

        setSize(900, 600)
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