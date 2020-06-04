package gui

import GUI
import config.Config
import getTimeAsClock
import getTimerState
import objects.OBSSceneTimer
import objects.OBSState
import objects.TimerState
import themes.Theme
import java.awt.*
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.EmptyBorder


class TimerPanel : JPanel(), Refreshable {
    private val logger = Logger.getLogger(TimerPanel::class.java.name)

    val sceneLabel: JLabel = JLabel("Initializing...")
    private val resetTimerButton = JButton("Reset")
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
        sceneLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 24)

        resetTimerButton.toolTipText = "Reset timer to 0"
        resetTimerButton.background = null
        resetTimerButton.requestFocus()
        resetTimerButton.cursor = Cursor(Cursor.HAND_CURSOR)
        resetTimerButton.isContentAreaFilled = false
        resetTimerButton.addActionListener {
            OBSSceneTimer.reset()
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
        timerUpLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, Config.timerCountUpFontSize)

        timerDownLabel.toolTipText = "Time remaining"
        timerDownLabel.horizontalAlignment = SwingConstants.CENTER
        timerDownLabel.alignmentX = Component.CENTER_ALIGNMENT
        timerDownLabel.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, Config.timerCountDownFontSize)
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

    override fun removeNotify() {
        super.removeNotify()
        GUI.unregister(this)
    }

    override fun refreshTimer() {
        updateLabelsForTimer()
        updateColorsForTimer()
        repaint()
    }

    override fun switchedScenes() {
        sceneLabel.text = OBSState.currentSceneName
    }

    private fun updateLabelsForTimer() {
        timerUpLabel.text = OBSSceneTimer.getTimerAsClock()

        if (OBSSceneTimer.getMaxTimerValue() > 0L) {
            val timeDifference = OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getValue()
            timerDownLabel.text = getTimeAsClock(timeDifference)
            timerDownLabel.isVisible = true
        } else {
            timerDownLabel.isVisible = false
        }
    }

    private fun updateColorsForTimer() {
        setColorsFor(getTimerState())
    }

    private fun setColorsFor(state: TimerState) {
        when (state) {
            TimerState.EXCEEDED -> {
                setLabelsColor(Theme.get.TIMER_EXCEEDED_FONT_COLOR)
                background = Theme.getTimerExceededBackgroundColor()
            }
            TimerState.APPROACHING -> {
                setLabelsColor(Theme.get.TIMER_APPROACHING_FONT_COLOR)
                background = Theme.getTimerApproachingBackgroundColor()
            }
            else -> {
                setLabelsColor(Theme.get.FONT_COLOR)
                background = Theme.getTimerDefaultBackgroundColor()
            }
        }
    }

    private fun setLabelsColor(color: Color) {
        sceneLabel.foreground = color
        resetTimerButton.foreground = color
        timerUpLabel.foreground = color
        timerDownLabel.foreground = color
    }
}