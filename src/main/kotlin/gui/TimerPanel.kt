package gui

import GUI
import config.Config
import getTimeAsClock
import objects.OBSSceneTimer
import java.awt.*
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.EmptyBorder


class TimerPanel : JPanel(), Refreshable {
    private val logger = Logger.getLogger(TimerPanel::class.java.name)

    val sceneLabel: JLabel = JLabel("Initializing...")
    val timerUpLabel: JLabel = JLabel()
    val timerDownLabel: JLabel = JLabel()

    init {
        GUI.register(this)
        initGUI()
        refreshTimer()
    }

    private fun initGUI() {
        setSize(800, 200)
        minimumSize = Dimension(0, 0)

        layout = BorderLayout(10, 10)
        border = EmptyBorder(10, 10, 10, 10)

        sceneLabel.horizontalAlignment = SwingConstants.CENTER
        sceneLabel.font = Font("Dialog", Font.PLAIN, 24)

        val resetTimerButton = JButton("Reset")
        resetTimerButton.toolTipText = "Reset timer to 0"
        resetTimerButton.background = null
        resetTimerButton.requestFocus()
        resetTimerButton.cursor = Cursor(Cursor.HAND_CURSOR)
        resetTimerButton.isContentAreaFilled = false
        resetTimerButton.addActionListener {
            OBSSceneTimer.resetTimer()
        }

        val topPanel = JPanel()
        topPanel.background = null
        topPanel.layout = BorderLayout(10, 10)
        topPanel.add(sceneLabel, BorderLayout.CENTER)
        topPanel.add(resetTimerButton, BorderLayout.LINE_END)
        add(topPanel, BorderLayout.PAGE_START)

        timerUpLabel.toolTipText = "Time elapsed"
        timerUpLabel.horizontalAlignment = SwingConstants.CENTER
        timerUpLabel.alignmentX = Component.CENTER_ALIGNMENT
        timerUpLabel.alignmentY = Component.CENTER_ALIGNMENT
        timerUpLabel.font = Font("Dialog", Font.PLAIN, Config.timerCountUpFontSize)

        timerDownLabel.toolTipText = "Time remaining"
        timerDownLabel.horizontalAlignment = SwingConstants.CENTER
        timerDownLabel.alignmentX = Component.CENTER_ALIGNMENT
        timerDownLabel.font = Font("Dialog", Font.PLAIN, Config.timerCountDownFontSize)
        timerDownLabel.isVisible = false

        val timersPanel = JPanel()
        timersPanel.background = null
        timersPanel.layout = BoxLayout(timersPanel, BoxLayout.PAGE_AXIS)
        timersPanel.add(Box.createVerticalGlue())
        timersPanel.add(timerUpLabel)
        timersPanel.add(Box.createRigidArea(Dimension(0, 20)))
        timersPanel.add(timerDownLabel)
        timersPanel.add(Box.createVerticalGlue())
        timersPanel.add(TimerProgressBarPanel())
        add(timersPanel, BorderLayout.CENTER)
    }

    override fun refreshTimer() {
        updateLabelsForTimer()
        updateBackgroundColorForTimer()
        repaint()
    }

    override fun switchedScenes() {
        sceneLabel.text = OBSSceneTimer.getCurrentSceneName()
    }

    private fun updateLabelsForTimer() {
        timerUpLabel.text = OBSSceneTimer.getTimerAsClock()

        if (OBSSceneTimer.getMaxTimerValue() > 0L) {
            val timeDifference = OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getTimerValue()
            timerDownLabel.text = getTimeAsClock(timeDifference)
            timerDownLabel.isVisible = true
        } else {
            timerDownLabel.isVisible = false
        }
    }

    private fun updateBackgroundColorForTimer() {
        val sceneMaxDuration = OBSSceneTimer.getMaxTimerValue()

        if (sceneMaxDuration == 0L) {
            background = Config.timerBackgroundColor
            return
        }

        if (OBSSceneTimer.getTimerValue() >= sceneMaxDuration) {
            logger.severe("Timer exceeded!")
            background = Config.exceededLimitColor

        } else if (sceneMaxDuration >= Config.largeMinLimitForLimitApproaching
            && OBSSceneTimer.getTimerValue() + Config.largeTimeDifferenceForLimitApproaching >= sceneMaxDuration
        ) {
            logger.severe("Timer almost exceeded!")
            background = Config.approachingLimitColor

        } else if (sceneMaxDuration < Config.largeMinLimitForLimitApproaching
            && sceneMaxDuration >= Config.smallMinLimitForLimitApproaching
            && OBSSceneTimer.getTimerValue() + Config.smallTimeDifferenceForLimitApproaching >= sceneMaxDuration
        ) {
            logger.severe("Timer almost exceeded!")
            background = Config.approachingLimitColor

        } else {
            background = Config.timerBackgroundColor
        }
    }

}