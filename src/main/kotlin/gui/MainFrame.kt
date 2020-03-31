package gui

import objects.OBSSceneTimer
import java.awt.Color
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

        setSize(600, 600)
        title = "Countdown Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
    }

    override fun refreshTimer() {
        title = "${OBSSceneTimer.getCurrentSceneName()}: ${OBSSceneTimer.getTimerAsClock()}"

        val sceneMaxDuration = OBSSceneTimer.getMaxTimerValue()

        if (sceneMaxDuration == 0L) {
            timerPanel?.background = Color.LIGHT_GRAY
            return
        }

        if (OBSSceneTimer.getTimerValue() >= sceneMaxDuration) {
            logger.severe("Timer exceeded!")
            timerPanel?.background = Color.RED
        } else if (sceneMaxDuration > 60 && OBSSceneTimer.getTimerValue() + 30 >= sceneMaxDuration) {
            logger.severe("Timer almost exceeded!")
            timerPanel?.background = Color.ORANGE
        } else if (sceneMaxDuration in 20..59 && OBSSceneTimer.getTimerValue() + 10 >= sceneMaxDuration) {
            logger.severe("Timer almost exceeded!")
            timerPanel?.background = Color.ORANGE
        } else {
            timerPanel?.background = Color.LIGHT_GRAY
        }
    }
}