package gui.sceneTable

import GUI
import config.Config
import gui.Refreshable
import objects.OBSSceneTimer
import objects.TScene
import obs.OBSState
import themes.Theme
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder


class SceneTablePanel : JPanel(), Refreshable {
    val sceneLabels = HashMap<String, JLabel>()
    val sceneInputs = HashMap<String, SceneInputPanel>()
    val container = JPanel()

    private val labelFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)
    private val groupedLabelFont = Font(labelFont.family, Font.ITALIC, labelFont.size)
    private val currentSceneLabelFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 16)
    private val currentSceneGroupedLabelFont =
        Font(currentSceneLabelFont.family, Font.BOLD + Font.ITALIC, currentSceneLabelFont.size)

    init {
        GUI.register(this)

        initGUI()
    }

    private fun initGUI() {
        layout = BorderLayout()

        container.border = EmptyBorder(0, 10, 0, 10)
        container.layout = GridLayout(0, 1)

        if (Config.remoteSyncClientEnabled) {
            val scenesNotLoadedLabel = JLabel("Scenes are not loaded in Client mode")
            scenesNotLoadedLabel.font = Font(Theme.get.FONT_FAMILY, Font.ITALIC, 16)
            scenesNotLoadedLabel.border = EmptyBorder(10, 0, 0, 10)
            container.add(scenesNotLoadedLabel)
        } else {
            createSceneTable()
        }

        val scrollPanelInnerPanel = JPanel(BorderLayout())
        scrollPanelInnerPanel.add(container, BorderLayout.PAGE_START)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
    }

    override fun removeNotify() {
        super.removeNotify()
        GUI.unregister(this)
    }

    private fun createSceneTable() {
        sceneLabels.clear()
        sceneInputs.clear()

        createSceneRowComponents()

        container.removeAll()

        container.add(
            createSceneTableRow(
                JLabel("Scene"),
                JLabel("Max duration (sec.)")
            )
        )

        if (OBSState.scenes.size == 0) {
            addNoScenesAvailableLabel(labelFont)
        } else {
            OBSState.scenes.forEach {
                container.add(
                    createSceneTableRow(
                        sceneLabels[it.name] ?: JLabel("[ unregistered scene ]"),
                        sceneInputs[it.name] ?: JTextField()
                    )
                )
            }

            refreshGroups()
        }
    }

    private fun createSceneRowComponents() {
        for (scene in OBSState.scenes) {
            sceneLabels[scene.name] = JLabel(scene.name)
            sceneInputs[scene.name] = SceneInputPanel(scene)
        }
    }

    private fun addNoScenesAvailableLabel(labelFont: Font) {
        val noScenesAvailableLabel = JLabel("No scenes available")
        noScenesAvailableLabel.font = labelFont
        noScenesAvailableLabel.horizontalAlignment = SwingConstants.CENTER
        noScenesAvailableLabel.alignmentX = Component.CENTER_ALIGNMENT

        container.add(Box.createRigidArea(Dimension(0, 30)))
        container.add(noScenesAvailableLabel)
    }

    private fun createSceneTableRow(sceneColumn: Component, inputColumn: Component): JPanel {
        val tableRow = JPanel()
        tableRow.layout = BorderLayout(10, 10)
        tableRow.add(sceneColumn, BorderLayout.CENTER)
        tableRow.add(inputColumn, BorderLayout.LINE_END)
        return tableRow
    }

    override fun refreshScenes() {
        createSceneTable()

        OBSSceneTimer.setMaxTimerValue(
            getValueForScene(OBSState.currentScene.name)
                .toLong()
        )
    }

    override fun switchedScenes() {
        if (Config.remoteSyncClientEnabled) {
            return
        }

        createSceneTable()

        OBSSceneTimer.setMaxTimerValue(
            getValueForScene(OBSState.currentScene.name)
                .toLong()
        )
    }

    private fun getValueForScene(sceneName: String): Int {
        return OBSState.scenes.find { it.name == sceneName }?.getFinalTimeLimit() ?: 0
    }

    override fun refreshGroups() {
        OBSState.scenes.forEach { scene ->
            val label = sceneLabels[scene.name] ?: return@forEach
            label.font = getLabelFontForScene(scene)

            if (scene.groups.isEmpty()) {
                label.toolTipText = null
            } else {
                label.toolTipText = "Group(s): " + scene.groups.joinToString(", ")
            }
        }
    }

    private fun getLabelFontForScene(scene: TScene): Font =
        if (scene.name == OBSState.currentScene.name) {
            if (scene.groups.isEmpty()) {
                currentSceneLabelFont
            } else {
                currentSceneGroupedLabelFont
            }
        } else {
            if (scene.groups.isEmpty()) {
                labelFont
            } else {
                groupedLabelFont
            }
        }
}
