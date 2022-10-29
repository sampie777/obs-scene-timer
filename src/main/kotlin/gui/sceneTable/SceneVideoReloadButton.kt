package gui.sceneTable

import GUI
import config.Config
import gui.Refreshable
import isAddressLocalhost
import objects.OBSSceneTimer
import objects.TScene
import obs.OBSClientStatus
import obs.OBSState
import utils.FAIcon
import java.awt.Color
import java.awt.EventQueue
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class SceneVideoReloadButton(private val scene: TScene) : JButton(), Refreshable {
    private var isVideoLengthLoading = false

    private val icon = FAIcon("\uf3e5", size = 12f).also { it.foreground = if (isEnabled) foreground else Color.RED }

    init {
        GUI.register(this)

        initGui()
    }

    private fun initGui() {
        border = CompoundBorder(
            BorderFactory.createLineBorder(Color(180, 180, 180)),
            EmptyBorder(5, 5, 5, 5),
        )
        background = null
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)

        add(icon)

        addActionListener { onClick() }

        refreshGui()
    }

    private fun refreshGui() {
        isEnabled = false
        if (!isAddressLocalhost(Config.obsAddress)) {
            toolTipText = "Make sure OBS is running on the same computer (localhost) to use this feature"
        } else if (OBSState.clientActivityStatus != OBSClientStatus.LOADING_SCENES && OBSState.clientActivityStatus != OBSClientStatus.LOADING_SCENE_SOURCES && !scene.sourcesAreLoaded) {
            toolTipText = "Please reload the scenes (Application > Reload scenes) to calculate max scene time"
        } else if (OBSState.clientActivityStatus == OBSClientStatus.LOADING_SCENES || OBSState.clientActivityStatus == OBSClientStatus.LOADING_SCENE_SOURCES || isVideoLengthLoading) {
            toolTipText = "Calculating media length..."
        } else if (scene.getFinalTimeLimit() == scene.maxVideoLength()) {
            toolTipText = "Time is already equal to max video length"
        } else {
            isEnabled = true
            toolTipText = "Reset time to max video length"
        }

        icon.isEnabled = isEnabled
    }

    private fun onClick() {
        scene.resetTimeLimit()
        GUI.onSceneTimeLimitUpdated(scene)

        if (scene.name == OBSState.currentScene.name) {
            OBSSceneTimer.setMaxTimerValue(scene.getFinalTimeLimit().toLong())
        }
    }

    override fun onSceneTimeLimitUpdated(scene: TScene) {
        EventQueue.invokeLater {
            refreshGui()
        }
    }
}