package gui

import GUI
import exitApplication
import gui.menu.MenuBar
import objects.OBSSceneTimer
import objects.OBSState
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URL
import java.util.logging.Logger
import javax.swing.JFrame

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val applicationIconDefault: Image?
    private val applicationIconRed: Image?

    init {
        GUI.register(this)

        applicationIconDefault = loadApplicationIcon("/icon-512.png")
        applicationIconRed = loadApplicationIcon("/icon-red-512.png")

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(winEvt: WindowEvent) {
                exitApplication()
            }
        })

        initGUI()
    }

    private fun initGUI() {
        add(MainFramePanel())

        jMenuBar = MenuBar()
        setSize(900, 600)
        title = "OBS Scene Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
        iconImage = applicationIconDefault
    }

    override fun refreshTimer() {
        title = "${OBSState.currentSceneName}: ${OBSSceneTimer.getTimerAsClock()}"

        if (OBSSceneTimer.getMaxTimerValue() > 0
            && OBSSceneTimer.getTimerValue() >= OBSSceneTimer.getMaxTimerValue()) {
            iconImage = applicationIconRed
        } else {
            iconImage = applicationIconDefault
        }
    }

    private fun loadApplicationIcon(iconPath: String): Image? {
        val resource: URL? = javaClass.getResource(iconPath)
        if (resource == null) {
            logger.warning("Could not find icon: $iconPath")
            return null
        }

        return Toolkit.getDefaultToolkit().getImage(resource)
    }
}