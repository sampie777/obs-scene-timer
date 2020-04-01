package gui

import GUI
import getTimeAsClock
import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder


class TimerPanel : JPanel(), Refreshable {

    private val sceneLabel: JLabel = JLabel()
    private val timerUpLabel: JLabel = JLabel()
    private val timerDownLabel: JLabel = JLabel()

    init {
        GUI.register(this)
        initGUI()
        refreshTimer()
    }

    private fun initGUI() {
        setSize(800, 200)

        layout = BorderLayout(10, 10)
        border = EmptyBorder(10, 10, 10, 10)

        sceneLabel.text = "Initializing..."
        sceneLabel.horizontalAlignment = SwingConstants.CENTER
        sceneLabel.font = Font("Dialog", Font.PLAIN, 24)
        add(sceneLabel, BorderLayout.NORTH)

        timerUpLabel.text = "Initializing..."
        timerUpLabel.horizontalAlignment = SwingConstants.CENTER
        timerUpLabel.alignmentX = Component.CENTER_ALIGNMENT
        timerUpLabel.alignmentY = Component.CENTER_ALIGNMENT
        timerUpLabel.font = Font("Dialog", Font.PLAIN, 80)

        timerDownLabel.text = "Initializing..."
        timerDownLabel.horizontalAlignment = SwingConstants.CENTER
        timerDownLabel.alignmentX = Component.CENTER_ALIGNMENT
        timerDownLabel.font = Font("Dialog", Font.PLAIN, 100)

        val timersPanel = JPanel()
        timersPanel.background = null
        timersPanel.layout = BoxLayout(timersPanel, BoxLayout.Y_AXIS)
        timersPanel.add(Box.createVerticalGlue())
        timersPanel.add(timerUpLabel)
        timersPanel.add(Box.createRigidArea(Dimension(0, 20)))
        timersPanel.add(timerDownLabel)
        timersPanel.add(Box.createVerticalGlue())
        add(timersPanel, BorderLayout.CENTER)
    }

    override fun refreshTimer() {
        sceneLabel.text = OBSSceneTimer.getCurrentSceneName()

        timerUpLabel.text = OBSSceneTimer.getTimerAsClock()

        if (OBSSceneTimer.getMaxTimerValue() > 0L) {
            val timeDifference = OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getTimerValue()
            timerDownLabel.text = getTimeAsClock(timeDifference)
        } else {
            timerDownLabel.text = ""
        }

        repaint()
    }

}