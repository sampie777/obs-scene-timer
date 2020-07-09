package gui.menu

import gui.menu.submenu.RemoteSyncMenu
import gui.utils.getMainFrameComponent
import gui.websocketScanner.WebsocketScannerFrame
import themes.Theme
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.KeyStroke

class ToolsMenu : JMenu("Tools") {
    private val logger = Logger.getLogger(ToolsMenu::class.java.name)

    init {
        initGui()
    }

    private fun initGui() {
        popupMenu.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)
        mnemonic = KeyEvent.VK_T

        val scannerItem = JMenuItem("Network Scanner")
        val remoteSyncItem = RemoteSyncMenu()

        scannerItem.mnemonic = KeyEvent.VK_W
        scannerItem.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK or InputEvent.ALT_MASK)
        remoteSyncItem.mnemonic = KeyEvent.VK_R

        scannerItem.addActionListener { WebsocketScannerFrame(getMainFrameComponent(this)) }

        add(scannerItem)
        add(remoteSyncItem)
    }
}