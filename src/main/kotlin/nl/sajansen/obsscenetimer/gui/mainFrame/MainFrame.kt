package nl.sajansen.obsscenetimer.gui.mainFrame

import nl.sajansen.obsscenetimer.ApplicationInfo
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.gui.menu.MenuBar
import nl.sajansen.obsscenetimer.gui.utils.loadIcon
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.TimerState
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSConnectionStatus
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import nl.sajansen.obsscenetimer.remotesync.client.TimerClient
import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import org.bridj.Pointer
import org.bridj.cpp.com.COMRuntime
import org.bridj.cpp.com.shell.ITaskbarList3
import org.bridj.jawt.JAWTUtils
import java.awt.EventQueue
import java.awt.Image
import java.util.*
import java.util.logging.Logger
import javax.swing.JFrame
import kotlin.math.max


class MainFrame : JFrame(), Refreshable, RemoteSyncRefreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val applicationIconDefault: Image?
    private val applicationIconOrange: Image?
    private val applicationIconRed: Image?
    private var taskbarList: ITaskbarList3? = null
    private var hwnd: Pointer<Int>? = null

    private val remoteSyncIndeterminateStates = arrayOf(
        ConnectionState.CONNECTING,
        ConnectionState.RECONNECTING,
        ConnectionState.CONNECTION_FAILED
    )
    private val oBSIndeterminateStates = arrayOf(
        OBSConnectionStatus.CONNECTING,
        OBSConnectionStatus.RECONNECTING,
        OBSConnectionStatus.CONNECTION_FAILED
    )

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
        RemoteSyncRefreshableRegister.register(this)

        applicationIconDefault = loadIcon("/nl/sajansen/obsscenetimer/icon-512.png")
        applicationIconOrange = loadIcon("/nl/sajansen/obsscenetimer/icon-orange-512.png")
        applicationIconRed = loadIcon("/nl/sajansen/obsscenetimer/icon-red-512.png")

        addWindowListener(MainFrameWindowAdapter(this))

        initGUI()

        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) {
            logger.info("Creating taskbar object")
            try {
                taskbarList = COMRuntime.newInstance(ITaskbarList3::class.java)
            } catch (t: Throwable) {
                logger.warning("Could no create tasbkar object for using taskbar progressbar")
                t.printStackTrace()
            }
        } else {
            logger.info("Cannot create taskbar object because system is not Windows but: ${System.getProperty("os.name")}")
        }
    }

    private fun initGUI() {
        add(MainFramePanel())

        if (Config.windowRestoreLastPosition) {
            location = Config.mainWindowLocation
            val newSize = Config.mainWindowSize
            newSize.width = max(newSize.width, 60)
            newSize.height = max(newSize.height, 60)
            size = newSize

            if (Config.mainWindowsIsMaximized) {
                extendedState = extendedState or MAXIMIZED_BOTH
            }

            setFullscreen(Config.mainWindowsIsFullscreen)
        } else {
            setSize(900, 600)
        }

        isAlwaysOnTop = Config.mainWindowAlwaysOnTop

        jMenuBar = MenuBar()
        title = ApplicationInfo.name
        defaultCloseOperation = EXIT_ON_CLOSE
        iconImage = applicationIconDefault
    }

    fun rebuildGui() {
        logger.info("Rebuilding main GUI")

        isAlwaysOnTop = Config.mainWindowAlwaysOnTop

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

        updateTaskbarProgressbar()
    }

    override fun refreshOBSStatus() {
        updateTaskbarProgressbar()
    }

    override fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        updateTaskbarProgressbar()
    }

    private fun updateTaskbarProgressbar() {
        // Check if taskbarlist is ever created (and thus also if system is Windows)
        if (taskbarList == null) {
            return
        }

        if (hwnd == null) {
            logger.info("Getting window handle")
            try {
                val hwndVal = JAWTUtils.getNativePeerHandle(this)
                @Suppress("UNCHECKED_CAST", "DEPRECATION")
                hwnd = Pointer.pointerToAddress(hwndVal) as Pointer<Int>?
            } catch (t: Throwable) {
                logger.warning("Could no get window handle for using taskbar progressbar")
                t.printStackTrace()
                return
            }
        }

        try {
            val progressbarFlag = getTaskbarProgressbarFlag()
            taskbarList?.SetProgressState(hwnd, progressbarFlag)

            if (progressbarFlag in arrayOf(
                    ITaskbarList3.TbpFlag.TBPF_ERROR,
                    ITaskbarList3.TbpFlag.TBPF_PAUSED,
                    ITaskbarList3.TbpFlag.TBPF_NORMAL
                )
            ) {
                taskbarList?.SetProgressValue(hwnd, OBSSceneTimer.getValue(), OBSSceneTimer.getMaxTimerValue())
            }
        } catch (t: Throwable) {
            logger.warning("Could no update taskbar progressbar")
            t.printStackTrace()
        }
    }

    private fun getTaskbarProgressbarFlag(): ITaskbarList3.TbpFlag {
        if (Config.remoteSyncClientEnabled && remoteSyncIndeterminateStates.contains(TimerClient.getConnectionState())) {
            return ITaskbarList3.TbpFlag.TBPF_INDETERMINATE
        }

        if (!Config.remoteSyncClientEnabled && oBSIndeterminateStates.contains(OBSState.connectionStatus)) {
            return ITaskbarList3.TbpFlag.TBPF_INDETERMINATE
        }

        if (OBSSceneTimer.getMaxTimerValue() == 0L) {
            return ITaskbarList3.TbpFlag.TBPF_NOPROGRESS
        }

        return when (OBSSceneTimer.getTimerState()) {
            TimerState.EXCEEDED -> ITaskbarList3.TbpFlag.TBPF_ERROR // Red
            TimerState.APPROACHING -> ITaskbarList3.TbpFlag.TBPF_PAUSED // Orange
            else -> ITaskbarList3.TbpFlag.TBPF_NORMAL   // Green
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