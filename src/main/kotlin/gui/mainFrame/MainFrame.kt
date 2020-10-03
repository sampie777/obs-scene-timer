package gui.mainFrame

import GUI
import config.Config
import gui.Refreshable
import gui.menu.MenuBar
import gui.utils.loadIcon
import objects.ApplicationInfo
import objects.OBSSceneTimer
import objects.TimerState
import objects.notifications.Notifications
import org.bridj.Pointer
import org.bridj.cpp.com.COMRuntime
import org.bridj.cpp.com.shell.ITaskbarList3
import org.bridj.jawt.JAWTUtils
import java.awt.EventQueue
import java.awt.Image
import java.util.logging.Logger
import javax.swing.JFrame


class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val applicationIconDefault: Image?
    private val applicationIconOrange: Image?
    private val applicationIconRed: Image?
    private var taskbarList: ITaskbarList3? = null
    private var hwnd: Pointer<Int>? = null

    companion object {
        private var instance: MainFrame? = null
        fun getInstance() = instance

        fun create(): MainFrame = MainFrame()

        fun createAndShow(): MainFrame {
            val frame = create()
            frame.isVisible = true
            return frame
        }
    }

    init {
        instance = this

        GUI.register(this)

        applicationIconDefault = loadIcon("/icon-512.png")
        applicationIconOrange = loadIcon("/icon-orange-512.png")
        applicationIconRed = loadIcon("/icon-red-512.png")

        addWindowListener(MainFrameWindowAdapter(this))

        initGUI()

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            logger.info("Creating taskbar object")
            taskbarList = COMRuntime.newInstance(ITaskbarList3::class.java)
        }
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
        title = ApplicationInfo.name
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = applicationIconDefault
    }

    fun rebuildGui() {
        logger.info("Rebuilding main GUI")
        EventQueue.invokeLater {
            contentPane.removeAll()

            add(MainFramePanel())
            jMenuBar = MenuBar()

            revalidate()
            repaint()
            logger.info("GUI rebuild done")
        }
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
        title = WindowTitle.generateWindowTitle()

        iconImage = when (OBSSceneTimer.getTimerState()) {
            TimerState.EXCEEDED -> applicationIconRed
            TimerState.APPROACHING -> applicationIconOrange
            else -> applicationIconDefault
        }

        // Check if taskbarlist is ever created (and thus also if system is Windows)
        if (taskbarList == null) {
            return
        }

        if (hwnd == null) {
            logger.info("Getting window handle")
            val hwndVal = JAWTUtils.getNativePeerHandle(this)
            hwnd = Pointer.pointerToAddress(hwndVal) as Pointer<Int>?
        }

        logger.info("Setting taskbar progress value")
        taskbarList?.SetProgressValue(hwnd, 50, 100)
        taskbarList?.SetProgressState(hwnd, ITaskbarList3.TbpFlag.TBPF_NORMAL)
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