package gui.sceneTable

import GUI
import getTimeAsClock
import objects.OBSSceneTimer
import objects.TScene
import obs.OBSState
import utils.FAIcon
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class SceneVideoReloadButton(private val scene: TScene) : JButton() {
    init {
        initGui()
    }

    private fun initGui() {
        border = CompoundBorder(
            BorderFactory.createLineBorder(Color(180, 180, 180)),
            EmptyBorder(5, 5, 5, 5),
        )
        background = null
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)

        toolTipText = "<html>Reset time to auto calculate max video length<br/>(that will be ${getTimeAsClock(scene.maxVideoLength().toLong(), looseFormat = true)})</html>"

        add(FAIcon("\uf3e5", fontSize = 12f).also { it.foreground = if (isEnabled) foreground else Color.RED })

        addActionListener { onClick() }
    }

    private fun onClick() {
        scene.resetTimeLimit()
        GUI.onSceneTimeLimitUpdated(scene)

        if (scene.name == OBSState.currentScene.name) {
            OBSSceneTimer.setMaxTimerValue(scene.getFinalTimeLimit().toLong())
        }
    }
}