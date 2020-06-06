package gui

import GUI
import config.Config
import exitApplication
import gui.menu.MenuBar
import gui.utils.loadIcon
import objects.OBSSceneTimer
import objects.OBSState
import objects.notifications.Notifications
import java.awt.Component
import java.awt.Image
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.logging.Logger
import javax.swing.JFrame

class MainFrameWindowAdapter(private val frame: MainFrame) : WindowAdapter() {
    override fun windowClosing(winEvt: WindowEvent) {
        frame.saveWindowPosition()
        GUI.windowClosing(frame)
        exitApplication()
    }
}

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val applicationIconDefault: Image?
    private val applicationIconRed: Image?

    companion object {
        fun create(): MainFrame = MainFrame()

        fun createAndShow(): MainFrame {
            val frame = create()
            frame.isVisible = true
            return frame
        }
    }

    init {
        GUI.register(this)

        applicationIconDefault = loadIcon("/icon-512.png")
        applicationIconRed = loadIcon("/icon-red-512.png")

        addWindowListener(MainFrameWindowAdapter(this))

        initGUI()
    }

    private fun initGUI() {
        add(MainFramePanel())

        if (Config.windowRestoreLastPosition) {
            location = Config.mainWindowLocation
            size = Config.mainWindowSize

            if (Config.mainWindowsIsMaximized) {
                extendedState = extendedState or MAXIMIZED_BOTH
            }

            setFullscreen(Config.mainWindowsIsFullscreen)
        } else {
            setSize(900, 600)
        }

        jMenuBar = MenuBar()
        title = "OBS Scene Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = applicationIconDefault
    }

    fun saveWindowPosition() {
        Config.mainWindowLocation = location

        if (extendedState == MAXIMIZED_BOTH) {
            Config.mainWindowsIsMaximized = true
        } else {
            Config.mainWindowsIsMaximized = false
            Config.mainWindowSize = size
        }
    }

    override fun refreshTimer() {
        title = "${OBSState.currentSceneName}: ${OBSSceneTimer.getTimerAsClock()}"

        if (OBSSceneTimer.getMaxTimerValue() > 0
            && OBSSceneTimer.getValue() >= OBSSceneTimer.getMaxTimerValue()) {
            iconImage = applicationIconRed
        } else {
            iconImage = applicationIconDefault
        }
    }

    fun toggleFullscreen() {
        Config.mainWindowsIsFullscreen = !Config.mainWindowsIsFullscreen

        setFullscreen(Config.mainWindowsIsFullscreen)
    }

    private fun setFullscreen(value: Boolean) {
        val graphicsDevice = graphicsConfiguration.device

        if (value) {
            logger.info("Enabling fullscreen")
            if (!graphicsDevice.isFullScreenSupported) {
                logger.info("Fullscreen not supported on this graphics device: $graphicsDevice")
                Notifications.add("Fullscreen is not supported by your graphics device", "GUI")
                return
            }

            graphicsDevice.fullScreenWindow = this
        } else {
            logger.info("Disabling fullscreen")
            if (graphicsDevice.fullScreenWindow == this) {
                graphicsDevice.fullScreenWindow = null
            }
        }
    }
}