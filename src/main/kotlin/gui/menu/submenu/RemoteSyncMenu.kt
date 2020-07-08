package gui.menu.submenu


import config.Config
import gui.MainFrame
import objects.ApplicationInfo
import objects.OBSClient
import objects.OBSSceneTimer
import objects.notifications.Notifications
import remotesync.RemoteSyncRefreshableRegister
import remotesync.client.TimerClient
import remotesync.objects.ConnectionState
import remotesync.objects.RemoteSyncRefreshable
import remotesync.server.ServerStatus
import remotesync.server.TimerServer
import themes.Theme
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem

class RemoteSyncMenu : JMenu("Remote sync"), RemoteSyncRefreshable {
    private val logger = Logger.getLogger(RemoteSyncMenu::class.java.name)

    val startServerItem = JMenuItem("Start server")
    val stopServerItem = JMenuItem("Stop server")
    val startClientItem = JMenuItem("Start client")
    val stopClientItem = JMenuItem("Stop client")

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

        startServerItem.mnemonic = KeyEvent.VK_S
        stopServerItem.mnemonic = KeyEvent.VK_S
        startClientItem.mnemonic = KeyEvent.VK_C
        stopClientItem.mnemonic = KeyEvent.VK_C

        add(startServerItem)
        add(stopServerItem)
        add(startClientItem)
        add(stopClientItem)

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
            logger.severe("Failed to start tread for stopping OBS and connecting to remote sync server")
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
}