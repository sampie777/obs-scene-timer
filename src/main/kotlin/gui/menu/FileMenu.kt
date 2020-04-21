package gui.menu

import config.Config
import gui.InfoFrame
import gui.utils.getMainFrameComponent
import gui.notifications.NotificationFrame
import javax.swing.JMenu
import javax.swing.JMenuItem
import kotlin.system.exitProcess

class FileMenu : JMenu("File") {
    init {
        initGui()
    }

    private fun initGui() {
        val notificationsItem = JMenuItem("Notifications")
        val infoItem = JMenuItem("Info")
        val quitItem = JMenuItem("Quit")

        notificationsItem.addActionListener { NotificationFrame(getMainFrameComponent(this)) }
        infoItem.addActionListener { InfoFrame(getMainFrameComponent(this)) }
        quitItem.addActionListener {
            Config.save()
            exitProcess(0)
        }

        add(notificationsItem)
        add(infoItem)
        add(quitItem)
    }
}