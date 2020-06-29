package gui.menu

import gui.menu.submenu.RemoteSyncMenu
import gui.utils.getMainFrameComponent
import gui.websocketScanner.WebsocketScannerFrame
import themes.Theme
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem

class ToolsMenu : JMenu("Tools") {
    private val logger = Logger.getLogger(ToolsMenu::class.java.name)

    init {
        initGui()
    }

    private fun initGui() {
        popupMenu.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)

        val scannerItem = JMenuItem("Network Scanner")
        val remoteSyncItem = RemoteSyncMenu()

        scannerItem.addActionListener { WebsocketScannerFrame(getMainFrameComponent(this)) }

        add(scannerItem)
        add(remoteSyncItem)
    }
}