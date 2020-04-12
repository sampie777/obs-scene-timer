package gui

import GUI
import config.Config
import objects.Globals
import objects.OBSSceneTimer
import java.awt.*
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
        panel.sceneValues[scene] = newValue
        Config.sceneLimitValues[scene] = newValue

        if (scene == OBSSceneTimer.getCurrentSceneName()) {
            OBSSceneTimer.setMaxTimerValue(newValue.toLong())
        }
    }
}

class SceneTablePanel : JPanel(), Refreshable {
    private val sceneLabels = HashMap<String, JLabel>()
    private val sceneInputs = HashMap<String, JSpinner>()
    val sceneValues = HashMap<String, Int>()
    private val container = JPanel()

    init {
        GUI.register(this)

        for (scene in Config.sceneLimitValues.keys) {
            sceneValues[scene] = Config.sceneLimitValues[scene] as Int
        }

        initGUI()
    }

    private fun initGUI() {
        layout = BorderLayout(0, 0)
        border = EmptyBorder(0, 10, 10, 10)

        createSceneTable()

        val scrollPanelInnerPanel = JPanel()
        scrollPanelInnerPanel.add(container)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
    }

    private fun createSceneTable() {
        val labelFont = Font("Dialog", Font.PLAIN, 16)
        val currentSceneLabelFont = Font("Dialog", Font.BOLD, 16)
        val inputFont = Font("Dialog", Font.PLAIN, 16)
        val currentSceneInputFont = Font("Dialog", Font.BOLD, 16)

        sceneLabels.clear()
        sceneInputs.clear()

        for (scene in Globals.scenes.values) {
            if (!sceneValues.containsKey(scene.name)) {
                sceneValues[scene.name] = 0
            }

            val sceneLabel = JLabel(scene.name)
            sceneLabel.font =
                if (scene.name == OBSSceneTimer.getCurrentSceneName()) currentSceneLabelFont else labelFont
            sceneLabels[scene.name] = sceneLabel

            val sceneInput = JSpinner()
            sceneInput.preferredSize = Dimension(100, 20)
            sceneInput.model = SpinnerNumberModel(sceneValues[scene.name], 0, null, 1)
            sceneInput.addChangeListener(SceneInputChangeListener(this, scene.name))
            sceneInput.font =
                if (scene.name == OBSSceneTimer.getCurrentSceneName()) currentSceneInputFont else inputFont
            sceneInputs[scene.name] = sceneInput
        }

        container.removeAll()
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)

        addSceneTableRow(
            JLabel("Scene"),
            JLabel("Duration (sec.)")
        )

        Globals.scenes.values.stream()
            .sorted { tScene, tScene2 -> tScene.name.compareTo(tScene2.name) }
            .forEach {
                addSceneTableRow(
                    sceneLabels[it.name] ?: JLabel("[ unregistered scene ]"),
                    sceneInputs[it.name] ?: JSpinner()
                )
            }
    }

    private fun addSceneTableRow(sceneColumn: Component, inputColumn: Component) {
        val tableRow = JPanel()
        tableRow.layout = BorderLayout(10, 10)
        tableRow.add(sceneColumn, BorderLayout.CENTER)
        tableRow.add(inputColumn, BorderLayout.LINE_END)
        container.add(tableRow)
    }

    override fun refreshScenes() {
        createSceneTable()
    }

    override fun switchedScenes() {
        OBSSceneTimer.setMaxTimerValue(
            getValueForScene(OBSSceneTimer.getCurrentSceneName())
                .toLong()
        )
    }

    fun getValueForScene(scene: String): Int {
        if (!sceneInputs.containsKey(scene)) {
            return 0
        }

        return sceneInputs[scene]?.value as Int
    }
}
