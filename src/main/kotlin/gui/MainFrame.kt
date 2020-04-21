package gui

import GUI
import createImageIcon
import gui.menu.MenuBar
import gui.notifications.NotificationFrame
import objects.OBSSceneTimer
import objects.notifications.Notifications
import java.awt.*
import java.net.URL
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainFrame : JFrame(), Refreshable {
    private val logger = Logger.getLogger(MainFrame::class.java.name)

    private val notificationsButton = JButton(createImageIcon("/notification-bell-24.png"))

    private val applicationIconDefault: Image?
    private val applicationIconRed: Image?

    init {
        GUI.register(this)

        applicationIconDefault = loadApplicationIcon("/icon-512.png")
        applicationIconRed = loadApplicationIcon("/icon-red-512.png")

        initGUI()

        refreshNotifications()
    }

    private fun initGUI() {
        notificationsButton.isBorderPainted = false
        notificationsButton.isContentAreaFilled = false
        notificationsButton.isFocusPainted = false
        notificationsButton.cursor = Cursor(Cursor.HAND_CURSOR)
        notificationsButton.toolTipText = "Notifications"
        notificationsButton.addActionListener {
            NotificationFrame(this)
        }

        val leftBottomPanel = JPanel(BorderLayout(10, 10))
        leftBottomPanel.border = EmptyBorder(10, 10, 10, 10)
        leftBottomPanel.minimumSize = Dimension(0, 0)
        leftBottomPanel.add(OBSStatusPanel(), BorderLayout.LINE_START)
        leftBottomPanel.add(notificationsButton, BorderLayout.LINE_END)

        val leftPanel = JPanel(BorderLayout(10, 10))
        leftPanel.add(SceneTablePanel(), BorderLayout.CENTER)
        leftPanel.add(leftBottomPanel, BorderLayout.PAGE_END)

        val mainPanel = JSplitPane()
        mainPanel.leftComponent = leftPanel
        mainPanel.rightComponent = TimerPanel()
        add(mainPanel)

        jMenuBar = MenuBar()
        setSize(900, 600)
        title = "OBS Scene Timer"
        defaultCloseOperation = EXIT_ON_CLOSE
        isVisible = true
        iconImage = applicationIconDefault
    }

    override fun refreshTimer() {
        title = "${OBSSceneTimer.getCurrentSceneName()}: ${OBSSceneTimer.getTimerAsClock()}"

        if (OBSSceneTimer.getMaxTimerValue() > 0
            && OBSSceneTimer.getTimerValue() >= OBSSceneTimer.getMaxTimerValue()) {
            iconImage = applicationIconRed
        } else {
            iconImage = applicationIconDefault
        }
    }

    override fun refreshNotifications() {
        if (Notifications.unreadNotifications > 0) {
            notificationsButton.text = "(${Notifications.unreadNotifications})"
            return
        }

        notificationsButton.text = ""
    }

    private fun loadApplicationIcon(iconPath: String): Image? {
        val resource: URL? = javaClass.getResource(iconPath)
        if (resource == null) {
            logger.warning("Could not find icon: $iconPath")
            return null
        }

        return Toolkit.getDefaultToolkit().getImage(resource)
    }
}