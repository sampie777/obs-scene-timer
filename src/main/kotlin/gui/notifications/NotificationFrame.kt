package gui.notifications

import gui.Refreshable
import objects.notifications.Notifications
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane

class NotificationFrame : JFrame(), Refreshable {

    init {
        GUI.register(this)

        createGui()

        Notifications.markAllAsRead()
    }

    private fun createGui() {
        val mainPanel = JPanel()
        mainPanel.layout = BorderLayout(0, 0)
        add(mainPanel)

        val scrollPanel = JScrollPane(NotificationListPanel())
        scrollPanel.border = null
        mainPanel.add(scrollPanel)

        title = "Notifications"
        setSize(420, 500)
        isVisible = true
    }
}