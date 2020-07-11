package gui.menu

import gui.grouping.GroupingFrame
import gui.mainFrame.MainFrame
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

        val groupingItem = JMenuItem("Group settings")
        val scannerItem = JMenuItem("Network Scanner")
        val remoteSyncItem = RemoteSyncMenu()

        groupingItem.mnemonic = KeyEvent.VK_G
        groupingItem.accelerator = KeyStroke.getKeyStroke(groupingItem.mnemonic, InputEvent.CTRL_MASK or InputEvent.ALT_MASK)
        scannerItem.mnemonic = KeyEvent.VK_W
        scannerItem.accelerator = KeyStroke.getKeyStroke(groupingItem.mnemonic, InputEvent.CTRL_MASK or InputEvent.ALT_MASK)
        remoteSyncItem.mnemonic = KeyEvent.VK_R

        groupingItem.addActionListener { GroupingFrame.createAndShow(MainFrame.getInstance()) }
        scannerItem.addActionListener { WebsocketScannerFrame(getMainFrameComponent(this)) }

        add(groupingItem)
        add(scannerItem)
        add(remoteSyncItem)
    }
}