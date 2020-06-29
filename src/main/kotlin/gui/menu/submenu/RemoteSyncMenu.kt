package gui.menu.submenu


import config.Config
import gui.MainFrame
import objects.OBSClient
import objects.notifications.Notifications
import remotesync.RemoteSyncRefreshableRegister
import remotesync.client.TimerClient
import remotesync.objects.ConnectionState
import remotesync.objects.RemoteSyncRefreshable
import remotesync.server.TimerServer
import themes.Theme
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem

class RemoteSyncMenu : JMenu("Remote sync"), RemoteSyncRefreshable {
    private val logger = Logger.getLogger(RemoteSyncMenu::class.java.name)

    private val startServerItem = JMenuItem("Start server")
    private val stopServerItem = JMenuItem("Stop server")
    private val startClientItem = JMenuItem("Start client")
    private val stopClientItem = JMenuItem("Stop client")

    init {
        RemoteSyncRefreshableRegister.register(this)
        initGui()
    }

    private fun initGui() {
        popupMenu.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)

        startServerItem.addActionListener {
            logger.info("Enabling remote sync server")
            Config.remoteSyncServerEnabled = true
            if (Config.remoteSyncClientEnabled) {
                Config.remoteSyncClientEnabled = false
                TimerClient.disconnect()
            }

            TimerServer.startServer()

            updateEnabledStatus()
            MainFrame.getInstance()?.rebuildGui()
            Notifications.popup("Please restart the application to (re)connect to OBS", "Remote Sync")
        }

        stopServerItem.addActionListener {
            logger.info("Disabling remote sync server")
            Config.remoteSyncServerEnabled = false
            TimerServer.stopServer()

            updateEnabledStatus()
        }

        startClientItem.addActionListener {
            logger.info("Enabling remote sync client")
            Config.remoteSyncClientEnabled = true
            if (Config.remoteSyncServerEnabled) {
                Config.remoteSyncServerEnabled = false
                TimerServer.stopServer()
            }
            OBSClient.stop()
            TimerClient.connect(Config.remoteSyncClientAddress)
//            Notifications.popup("Please restart the application for the changes to take effect", "Remote Sync")

            updateEnabledStatus()
            MainFrame.getInstance()?.rebuildGui()
        }

        stopClientItem.addActionListener {
            logger.info("Disabling remote sync client")
            Config.remoteSyncClientEnabled = false
            TimerClient.disconnect()
            updateEnabledStatus()
            Notifications.add("Please restart the application to (re)connect to OBS", "Remote Sync")
        }

        add(startServerItem)
        add(stopServerItem)
        add(startClientItem)
        add(stopClientItem)

        updateEnabledStatus()
    }

    private fun updateEnabledStatus() {
        startServerItem.isEnabled = !Config.remoteSyncServerEnabled
        stopServerItem.isEnabled = Config.remoteSyncServerEnabled
        startClientItem.isEnabled = !Config.remoteSyncClientEnabled
        stopClientItem.isEnabled = Config.remoteSyncClientEnabled
    }

    override fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        updateEnabledStatus()
    }

    override fun remoteSyncServerRefreshConnectionState() {
        updateEnabledStatus()
    }
}