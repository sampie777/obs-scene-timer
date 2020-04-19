package gui.notifications

import gui.Refreshable
import objects.notifications.Notifications
import java.util.logging.Logger
import javax.swing.BoxLayout
import javax.swing.JPanel

class NotificationListPanel : JPanel(), Refreshable {

    private val logger = Logger.getLogger(NotificationListPanel::class.java.name)

    private val mainPanel = JPanel()

    init {
        GUI.register(this)

        createGui()

        refreshNotifications()
    }

    private fun createGui() {
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        add(mainPanel)
    }

    private fun addNotificationPanels() {
        mainPanel.removeAll()

        Notifications.list.stream()
            .sorted { notification, notification2 -> notification2.timestamp.compareTo(notification.timestamp) }
            .forEach {
                mainPanel.add(NotificationPanel(it))
            }

        repaint()
        revalidate()
    }

    override fun refreshNotifications() {
        addNotificationPanels()
    }
}