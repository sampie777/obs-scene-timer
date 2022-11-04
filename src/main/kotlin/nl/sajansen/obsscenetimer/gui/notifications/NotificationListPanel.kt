package nl.sajansen.obsscenetimer.gui.notifications

import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.notifications.Notifications
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class NotificationListPanel : JPanel(), Refreshable {

    private val logger = LoggerFactory.getLogger(NotificationListPanel::class.java.name)

    private val mainPanel = JPanel()

    init {
        GUI.register(this)

        createGui()

        Notifications.markAllAsRead()
    }

    private fun createGui() {
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.PAGE_AXIS)
        add(mainPanel)
    }

    override fun removeNotify() {
        super.removeNotify()
        GUI.unregister(this)
    }

    private fun addNotificationPanels() {
        mainPanel.removeAll()

        if (Notifications.list.size == 0) {
            mainPanel.add(Box.createRigidArea(Dimension(0, 70)))
            mainPanel.add(JLabel("No notifications"))
        } else {
            Notifications.list.stream()
                .sorted { notification, notification2 -> notification2.timestamp.compareTo(notification.timestamp) }
                .forEach {
                    mainPanel.add(NotificationPanel(it))
                }
        }

        repaint()
        revalidate()
    }

    override fun refreshNotifications() {
        EventQueue.invokeLater {
            addNotificationPanels()
        }
    }
}