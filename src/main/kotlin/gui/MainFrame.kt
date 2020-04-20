package gui

import GUI
import gui.notifications.NotificationFrame
import objects.OBSSceneTimer
import objects.notifications.Notifications
import java.awt.BorderLayout
import java.awt.Toolkit
import java.util.logging.Logger
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val notificationsButton = JButton("Notifications")

    init {
        GUI.register(this)
        initGUI()

        refreshNotifications()
    }

    private fun initGUI() {
        notificationsButton.addActionListener {
            NotificationFrame()
        }

        val leftBottomPanel = JPanel(BorderLayout(10, 10))
        leftBottomPanel.border = EmptyBorder(10, 10, 10, 10)
        leftBottomPanel.add(OBSStatusPanel(), BorderLayout.LINE_START)
        leftBottomPanel.add(notificationsButton, BorderLayout.LINE_END)

        val leftPanel = JPanel(BorderLayout(10, 10))
        leftPanel.add(SceneTablePanel(), BorderLayout.CENTER)
        leftPanel.add(leftBottomPanel, BorderLayout.PAGE_END)

        val mainPanel = JPanel(BorderLayout(10, 10))
        add(mainPanel)
        mainPanel.add(leftPanel, BorderLayout.LINE_START)
        mainPanel.add(TimerPanel(), BorderLayout.CENTER)

        setSize(900, 600)
        title = "OBS Scene Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
        iconImage = Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/icon.png"))
    }

    override fun refreshTimer() {
        title = "${OBSSceneTimer.getCurrentSceneName()}: ${OBSSceneTimer.getTimerAsClock()}"
    }

    override fun refreshNotifications() {
        var notificationsAmount = ""
        if (Notifications.unreadNotifications > 0) {
            notificationsAmount = "(${Notifications.unreadNotifications})"
        }

        notificationsButton.text = "Notifications $notificationsAmount"
    }
}