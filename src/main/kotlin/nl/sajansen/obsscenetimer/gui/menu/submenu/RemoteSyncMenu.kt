package nl.sajansen.obsscenetimer.gui.menu.submenu


import nl.sajansen.obsscenetimer.ApplicationInfo
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.mainFrame.MainFrame
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import nl.sajansen.obsscenetimer.obs.OBSClient
import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import nl.sajansen.obsscenetimer.remotesync.client.TimerClient
import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import nl.sajansen.obsscenetimer.remotesync.server.ServerStatus
import nl.sajansen.obsscenetimer.remotesync.server.TimerServer
import nl.sajansen.obsscenetimer.themes.Theme
import nl.sajansen.obsscenetimer.utils.Rollbar
import openWebURL
import org.slf4j.LoggerFactory
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem

class RemoteSyncMenu : JMenu("Remote sync"), RemoteSyncRefreshable {
    private val logger = LoggerFactory.getLogger(RemoteSyncMenu::class.java.name)

    val startServerItem = JMenuItem("Start as server")
    val stopServerItem = JMenuItem("Stop as server")
    val startClientItem = JMenuItem("Start as client")
    val stopClientItem = JMenuItem("Stop as client")
    private val htmlClientItem = JMenuItem("Download HTML client")

    init {
        RemoteSyncRefreshableRegister.register(this)
        initGui()
    }

    private fun initGui() {
        popupMenu.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)

        startServerItem.addActionListener { startServer() }
        stopServerItem.addActionListener { stopServer() }
        startClientItem.addActionListener { startClient() }
        stopClientItem.addActionListener { stopClient() }
        htmlClientItem.addActionListener { downloadHTMLClient() }

        startServerItem.mnemonic = KeyEvent.VK_S
        stopServerItem.mnemonic = KeyEvent.VK_S
        startClientItem.mnemonic = KeyEvent.VK_C
        stopClientItem.mnemonic = KeyEvent.VK_C

        add(startServerItem)
        add(stopServerItem)
        add(startClientItem)
        add(stopClientItem)
        addSeparator()
        add(htmlClientItem)

        updateMenuItems()
    }

    fun updateMenuItems() {
        startServerItem.isEnabled = !Config.remoteSyncServerEnabled
        stopServerItem.isEnabled = Config.remoteSyncServerEnabled
        startClientItem.isEnabled = !Config.remoteSyncClientEnabled
        stopClientItem.isEnabled = Config.remoteSyncClientEnabled

        if (!Config.remoteSyncServerEnabled) {
            stopServerItem.toolTipText = ""
        } else {
            remoteSyncServerConnectionsUpdate()
        }
    }

    override fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        updateMenuItems()
    }

    override fun remoteSyncServerRefreshConnectionState() {
        updateMenuItems()
    }

    override fun remoteSyncServerConnectionsUpdate() {
        stopServerItem.toolTipText = "${ServerStatus.clients.size} connections"
    }

    private fun startServer() {
        logger.info("Enabling remote sync server")
        Config.remoteSyncServerEnabled = true
        if (Config.remoteSyncClientEnabled) {
            Config.remoteSyncClientEnabled = false
            TimerClient.disconnect()
        }

        TimerServer.startServer()

        updateMenuItems()
        MainFrame.getInstance()?.rebuildGui()

        if (!OBSClient.isRunning()) {
            Notifications.popup("Please restart the application to (re)connect to OBS", "Remote Sync")
        }
    }

    private fun stopServer() {
        logger.info("Disabling remote sync server")
        Config.remoteSyncServerEnabled = false
        TimerServer.stopServer()

        updateMenuItems()
    }

    private fun startClient() {
        logger.info("Enabling remote sync client")
        Config.remoteSyncClientEnabled = true
        if (Config.remoteSyncServerEnabled) {
            Config.remoteSyncServerEnabled = false
            TimerServer.stopServer()
        }

        OBSSceneTimer.stop()

        try {
            Thread {
                OBSClient.stop()

                TimerClient.connect(Config.remoteSyncClientAddress)
            }.start()
        } catch (e: Exception) {
            logger.error("Failed to start tread for stopping OBS and connecting to remote sync server. ${e.localizedMessage}")
            Rollbar.error(e, "Failed to start tread for stopping OBS and connecting to remote sync server")
            e.printStackTrace()
            Notifications.popup(
                "Could not setup connection to remote sync server: ${e.localizedMessage}. Try restarting ${ApplicationInfo.name}",
                "Remote Sync"
            )
        }

        updateMenuItems()
        MainFrame.getInstance()?.rebuildGui()
    }

    private fun stopClient() {
        logger.info("Disabling remote sync client")
        Config.remoteSyncClientEnabled = false
        TimerClient.disconnect()

        updateMenuItems()
        Notifications.add("Please restart the application to (re)connect to OBS", "Remote Sync")
    }

    private fun downloadHTMLClient() {
        logger.info("Opening HTML remote sync client")
        openWebURL(ApplicationInfo.url, "Download remote client")
    }
}