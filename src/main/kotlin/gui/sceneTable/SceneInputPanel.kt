package gui.sceneTable

import GUI
import config.Config
import gui.Refreshable
import isAddressLocalhost
import objects.TScene
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.JPanel

class SceneInputPanel(private val scene: TScene) : JPanel(), Refreshable {
    val sceneInput = SceneInput(scene)
    private val reloadPanel = JPanel()

    init {
        GUI.register(this)

        initGUI()
    }

    private fun initGUI() {
        layout = BorderLayout(8, 0)

        add(sceneInput, BorderLayout.CENTER)
        add(reloadPanel, BorderLayout.LINE_END)

        reloadPanel.preferredSize = Dimension(25, 0)
        reloadPanel.layout = BorderLayout()

        refreshReloadPanel()
    }

    private fun refreshReloadPanel() {
        reloadPanel.removeAll()

        if (!isAddressLocalhost(Config.obsAddress)) {
            // pass
        } else if (allSourceTimesAreLoaded()) {
            if (scene.timeLimit != null) {
                reloadPanel.add(SceneVideoReloadButton(scene), BorderLayout.CENTER)
            }
        } else {
            reloadPanel.add(SceneVideoLoadingIcon(), BorderLayout.CENTER)
        }

        reloadPanel.revalidate()
        reloadPanel.repaint()
    }

    private fun allSourceTimesAreLoaded() = scene.sourcesAreLoaded && scene.sources.all { it.settingsLoaded }

    override fun onSceneTimeLimitUpdated(scene: TScene) {
        if (scene != this.scene) return
        refreshScenes()
    }

    override fun refreshScenes() {
        EventQueue.invokeLater {
            refreshReloadPanel()
        }
    }
}