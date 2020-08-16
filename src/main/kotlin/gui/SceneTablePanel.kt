package gui

import GUI
import config.Config
import objects.OBSSceneTimer
import objects.OBSState
import objects.TScene
import themes.Theme
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class SceneInputChangeListener(private val scene: TScene) : ChangeListener {
    override fun stateChanged(p0: ChangeEvent?) {
        if (p0 == null) {
            return
        }

        val newValue = (p0.source as JSpinner).value as Int
        Config.sceneLimitValues[scene.name] = newValue
        scene.timeLimit = newValue

        if (scene.name == OBSState.currentScene.name) {
            OBSSceneTimer.setMaxTimerValue(newValue.toLong())
        }
    }
}

class SceneTablePanel : JPanel(), Refreshable {
    val sceneLabels = HashMap<String, JLabel>()
    val sceneInputs = HashMap<String, JSpinner>()
    val container = JPanel()

    private val labelFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)
    private val currentSceneLabelFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 16)
    private val inputFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 16)
    private val currentSceneInputFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 16)

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
                        sceneInputs[it.name] ?: JSpinner()
                    )
                )
            }
        }
    }

    private fun createSceneRowComponents() {
        for (scene in OBSState.scenes) {
            val sceneValue = if (!Config.sceneLimitValues.containsKey(scene.name))
                scene.maxVideoLength() else Config.sceneLimitValues[scene.name] as Int

            val sceneLabel = JLabel(scene.name)
            sceneLabel.font = if (scene.name == OBSState.currentScene.name)
                currentSceneLabelFont else labelFont
            sceneLabels[scene.name] = sceneLabel

            val sceneInput = JSpinner()
            sceneInput.preferredSize = Dimension(100, 22)
            sceneInput.model = SpinnerNumberModel(sceneValue, 0, null, 1)
            sceneInput.addChangeListener(SceneInputChangeListener(scene))
            sceneInput.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)
            sceneInput.font = if (scene.name == OBSState.currentScene.name)
                currentSceneInputFont else inputFont
            sceneInputs[scene.name] = sceneInput
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

    private fun getValueForScene(scene: String): Int {
        if (!sceneInputs.containsKey(scene)) {
            return 0
        }

        return sceneInputs[scene]?.value as Int
    }
}
