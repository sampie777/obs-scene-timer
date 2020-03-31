package gui

import getTimeAsClock
import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import javax.swing.*

class TimerPanel : JPanel(), Refreshable {

    private var sceneLabel: JLabel? = null
    private var timerUpLabel: JLabel? = null
    private var timerDownLabel: JLabel? = null

    init {
        GUI.register(this)
        initGUI()
        refreshTimer()
    }

    private fun initGUI() {
        setSize(600, 200)

        layout = BorderLayout(0, 0)

        sceneLabel = JLabel("Initializing...", SwingConstants.CENTER)
        sceneLabel!!.font = Font("Dialog", Font.PLAIN, 24)
        add(sceneLabel, BorderLayout.NORTH)

        timerUpLabel = JLabel("Initializing...", SwingConstants.CENTER)
        timerUpLabel!!.alignmentX = Component.CENTER_ALIGNMENT
        timerUpLabel!!.alignmentY = Component.CENTER_ALIGNMENT
        timerUpLabel!!.font = Font("Dialog", Font.PLAIN, 80)

        timerDownLabel = JLabel("Initializing...", SwingConstants.CENTER)
        timerDownLabel!!.alignmentX = Component.CENTER_ALIGNMENT
        timerDownLabel!!.font = Font("Dialog", Font.PLAIN, 60)

        val timersPanel = JPanel()
        timersPanel.background = null
        timersPanel.layout = BoxLayout(timersPanel, BoxLayout.Y_AXIS)
        timersPanel.add(Box.createVerticalGlue())
        timersPanel.add(timerUpLabel)
        timersPanel.add(timerDownLabel)
        timersPanel.add(Box.createVerticalGlue())
        add(timersPanel, BorderLayout.CENTER)
    }

    override fun refreshTimer() {
        sceneLabel?.text = OBSSceneTimer.getCurrentSceneName()

        timerUpLabel?.text = OBSSceneTimer.getTimerAsClock()

        if (OBSSceneTimer.getMaxTimerValue() > 0L) {
            val timeDifference = OBSSceneTimer.getMaxTimerValue() - OBSSceneTimer.getTimerValue()
            timerDownLabel?.text = getTimeAsClock(timeDifference)
        } else {
            timerDownLabel?.text = ""
        }

        repaint()
    }

}