package nl.sajansen.obsscenetimer.gui.notifications

import nl.sajansen.obsscenetimer.objects.notifications.Notification
import nl.sajansen.obsscenetimer.themes.Theme
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.text.SimpleDateFormat
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class NotificationPanel(private val notification: Notification) : JPanel() {
    init {
        createGui()
    }

    private fun createGui() {
        layout = BorderLayout(10, 10)

        border = CompoundBorder(
            CompoundBorder(
                EmptyBorder(5, 0, 5, 0),
                BorderFactory.createLineBorder(Color(180, 180, 180))
            ),
            EmptyBorder(10, 15, 10, 15)
        )

        val subjectLabel = JLabel(notification.subject.toUpperCase())
        subjectLabel.font = Font(Theme.get.FONT_FAMILY, Font.BOLD, 13)
        subjectLabel.foreground = Color(125, 125, 125)

        val timestampLabel = JLabel(SimpleDateFormat("HH:mm").format(notification.timestamp))
        timestampLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 10)
        timestampLabel.foreground = Color(125, 125, 125)

        val messageLabel = JLabel("<html><div style='width: 280px'>${notification.message}</div></html>")
        messageLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)

        val topPanel = JPanel(BorderLayout(10, 10))
        topPanel.add(subjectLabel, BorderLayout.LINE_START)
        topPanel.add(timestampLabel, BorderLayout.LINE_END)

        add(topPanel, BorderLayout.PAGE_START)
        add(messageLabel, BorderLayout.CENTER)
    }
}