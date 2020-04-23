package gui

import GUI
import config.Config
import objects.Globals
import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class SceneInputChangeListener(private val panel: SceneTablePanel, private val scene: String) : ChangeListener {
    override fun stateChanged(p0: ChangeEvent?) {
        if (p0 == null) {
            return
        }

        val newValue = (p0.source as JSpinner).value as Int
        Config.sceneLimitValues[scene] = newValue

        if (scene == OBSSceneTimer.getCurrentSceneName()) {
            OBSSceneTimer.setMaxTimerValue(newValue.toLong())
        }
    }
}

class SceneTablePanel : JPanel(), Refreshable {
    val sceneLabels = HashMap<String, JLabel>()
    val sceneInputs = HashMap<String, JSpinner>()
    val container = JPanel()

    private val labelFont = Font("Dialog", Font.PLAIN, 16)
    private val currentSceneLabelFont = Font("Dialog", Font.BOLD, 16)
    private val inputFont = Font("Dialog", Font.PLAIN, 16)
    private val currentSceneInputFont = Font("Dialog", Font.BOLD, 16)

    init {
        GUI.register(this)

        initGUI()
    }

    private fun initGUI() {
        layout = BorderLayout(0, 0)

        container.border = EmptyBorder(0, 10, 0, 10)
        container.layout = BoxLayout(container, BoxLayout.PAGE_AXIS)

        createSceneTable()

        val scrollPanelInnerPanel = JPanel()
        scrollPanelInnerPanel.add(container)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
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

        if (Globals.scenes.size == 0) {
            addNoScenesAvailableLabel(labelFont)
        } else {
            Globals.scenes.forEach {
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
        for (scene in Globals.scenes) {
            val sceneValue = if (!Config.sceneLimitValues.containsKey(scene.name))
                scene.maxVideoLength() else Config.sceneLimitValues[scene.name] as Int

            val sceneLabel = JLabel(scene.name)
            sceneLabel.font = if (scene.name == OBSSceneTimer.getCurrentSceneName())
                currentSceneLabelFont else labelFont
            sceneLabels[scene.name] = sceneLabel

            val sceneInput = JSpinner()
            sceneInput.preferredSize = Dimension(100, 20)
            sceneInput.model = SpinnerNumberModel(sceneValue, 0, null, 1)
            sceneInput.addChangeListener(SceneInputChangeListener(this, scene.name))
            sceneInput.font = if (scene.name == OBSSceneTimer.getCurrentSceneName())
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
    }

    override fun switchedScenes() {
        OBSSceneTimer.setMaxTimerValue(
            getValueForScene(OBSSceneTimer.getCurrentSceneName())
                .toLong()
        )

        createSceneTable()
    }

    private fun getValueForScene(scene: String): Int {
        if (!sceneInputs.containsKey(scene)) {
            return 0
        }

        return sceneInputs[scene]?.value as Int
    }
}
