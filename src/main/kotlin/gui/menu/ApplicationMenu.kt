package gui.menu

import exitApplication
import gui.config.ConfigFrame
import gui.mainFrame.MainFrame
import gui.notifications.NotificationFrame
import gui.utils.getMainFrameComponent
import themes.Theme
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.BorderFactory
import javax.swing.JMenu
import javax.swing.JMenuItem

class ApplicationMenu : JMenu("Application") {
    private val logger = Logger.getLogger(ApplicationMenu::class.java.name)

    init {
        initGui()
    }

    private fun initGui() {
        popupMenu.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)
        mnemonic = KeyEvent.VK_A

        val notificationsItem = JMenuItem("Notifications")
        val settingsItem = JMenuItem("Settings")
        val fullscreenItem = JMenuItem("Toggle fullscreen")
        val infoItem = JMenuItem("Info")
        val quitItem = JMenuItem("Quit")

        // Set alt keys
        notificationsItem.mnemonic = KeyEvent.VK_N
        settingsItem.mnemonic = KeyEvent.VK_S
        fullscreenItem.mnemonic = KeyEvent.VK_F
        infoItem.mnemonic = KeyEvent.VK_I
        quitItem.mnemonic = KeyEvent.VK_Q

        notificationsItem.addActionListener { NotificationFrame(getMainFrameComponent(this)) }
        settingsItem.addActionListener { ConfigFrame(getMainFrameComponent(this)) }
        fullscreenItem.addActionListener {
            (getMainFrameComponent(this) as MainFrame).toggleFullscreen()
        }
        infoItem.addActionListener { InfoFrame.createAndShow(getMainFrameComponent(this)) }
        quitItem.addActionListener { exitApplication() }

        add(notificationsItem)
        add(settingsItem)
        addSeparator()
        add(fullscreenItem)
        add(infoItem)
        add(quitItem)
    }
}