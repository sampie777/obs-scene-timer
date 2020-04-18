package gui

import GUI
import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.util.logging.Logger
import javax.swing.JFrame
import javax.swing.JPanel

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

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

        leftPanel.add(SceneTablePanel(), BorderLayout.CENTER)
        leftPanel.add(OBSStatusPanel(), BorderLayout.PAGE_END)

        mainPanel.add(leftPanel, BorderLayout.LINE_START)
        mainPanel.add(TimerPanel(), BorderLayout.CENTER)

        setSize(900, 600)
        title = "OBS Scene Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
    }

    override fun refreshTimer() {
        title = "${OBSSceneTimer.getCurrentSceneName()}: ${OBSSceneTimer.getTimerAsClock()}"
    }
}