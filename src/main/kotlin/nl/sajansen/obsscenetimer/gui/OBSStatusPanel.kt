package nl.sajansen.obsscenetimer.gui

import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.config.PropertyLoader
import nl.sajansen.obsscenetimer.obs.OBSConnectionStatus
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.remotesync.RemoteSyncRefreshableRegister
import nl.sajansen.obsscenetimer.remotesync.objects.ConnectionState
import nl.sajansen.obsscenetimer.remotesync.objects.RemoteSyncRefreshable
import nl.sajansen.obsscenetimer.themes.Theme
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel

class OBSStatusPanel : JPanel(), Refreshable, RemoteSyncRefreshable {

    private val messageLabel: JLabel = JLabel()

    init {
        GUI.register(this)
        RemoteSyncRefreshableRegister.register(this)

        initGUI()
        refreshOBSStatus()
    }

    private fun initGUI() {
        layout = BorderLayout(15, 15)

        messageLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 14)
        messageLabel.toolTipText = settingsFileString()
        add(messageLabel)
    }

    override fun removeNotify() {
        super.removeNotify()
        GUI.unregister(this)
        RemoteSyncRefreshableRegister.unregister(this)
    }

    fun getMessageLabel(): JLabel {
        return messageLabel
    }

    override fun refreshOBSStatus() {
        EventQueue.invokeLater {
            messageLabel.text = "OBS: ${getOBSStatusRepresentation()}"

            if (OBSState.connectionStatus == OBSConnectionStatus.CONNECTED) {
                messageLabel.toolTipText = "<html>Connected to ${Config.obsAddress}.<br/>${settingsFileString()}</html>"
            } else {
                messageLabel.toolTipText = settingsFileString()
            }
            repaint()
        }
    }

    fun getOBSStatusRepresentation(): String {
        var obsDisplayStatusString = if (OBSState.clientActivityStatus != null)
            OBSState.clientActivityStatus?.status ?: "" else OBSState.connectionStatus.status

        if (OBSState.clientActivityStatus == null && OBSState.connectionStatus == OBSConnectionStatus.CONNECTING) {
            obsDisplayStatusString = "Connecting to ${Config.obsAddress}..."
        }

        return obsDisplayStatusString
    }

    override fun remoteSyncClientRefreshConnectionState(state: ConnectionState) {
        if (!Config.remoteSyncClientEnabled) {
            return
        }

        EventQueue.invokeLater {
            messageLabel.text = "Remote sync: ${getRemoteSyncClientStatusRepresentation(state)}"

            if (state == ConnectionState.CONNECTED) {
                messageLabel.toolTipText = "<html>Connected to ${Config.remoteSyncServerHost}:${Config.remoteSyncServerPort}.<br/>${settingsFileString()}</html>"
            } else {
                messageLabel.toolTipText = settingsFileString()
            }
            repaint()
        }
    }

    private fun getRemoteSyncClientStatusRepresentation(state: ConnectionState): String {
        var remoteSyncClientDisplayStatusString = state.text
        if (state == ConnectionState.CONNECTING) {
            remoteSyncClientDisplayStatusString = "Connecting to ${Config.remoteSyncServerHost}:${Config.remoteSyncServerPort}..."
        }

        return remoteSyncClientDisplayStatusString
    }

    private fun settingsFileString(): String {
        return "Settings file: " + PropertyLoader.getPropertiesFile().absolutePath
    }
}