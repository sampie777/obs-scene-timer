package nl.sajansen.obsscenetimer.gui.sceneTable

import isAddressLocalhost
import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.obs.OBSClientStatus
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.themes.Theme
import nl.sajansen.obsscenetimer.utils.FAIcon
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.JPanel

class SceneInputPanel(private val scene: TScene) : JPanel(), Refreshable {
    val sceneInput = SceneInput(scene)
    val reloadPanel = JPanel()

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

        if (!isAddressLocalhost(Config.obsHost) || !Config.autoCalculateSceneLimitsBySources) {
            // pass
        } else if (scene.allSourceTimesAreLoaded()) {
            if (scene.timeLimit != null) {
                reloadPanel.add(SceneVideoReloadButton(scene), BorderLayout.CENTER)
            }
        } else if (OBSState.clientActivityStatus == OBSClientStatus.LOADING_SCENES
            || OBSState.clientActivityStatus == OBSClientStatus.LOADING_SCENE_SOURCES
            || scene.someSourceTimesAreLoading()
        ) {
            reloadPanel.add(SceneVideoLoadingIcon(), BorderLayout.CENTER)
        } else {
            reloadPanel.add(FAIcon("\uf071", fontSize = 12f).also {
                it.toolTipText = "Scene's items not loaded"
                it.foreground = Theme.get.WARNING_FONT_COLOR
            })
        }

        reloadPanel.revalidate()
        reloadPanel.repaint()
    }

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