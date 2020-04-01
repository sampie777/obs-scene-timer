package gui

import GUI
import objects.Globals
import objects.OBSSceneTimer
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
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
            if (scene.name == OBSSceneTimer.getCurrentSceneName()) {
                sceneLabel.font = currentSceneLabelFont
            } else {
                sceneLabel.font = labelFont
            }
            sceneLabels[scene.name] = sceneLabel

            val sceneInput = JSpinner()
            sceneInput.model = SpinnerNumberModel(sceneValues[scene.name], 0, null, 1)
            sceneInput.addChangeListener(SceneInputChangeListener(this, scene.name))
            if (scene.name == OBSSceneTimer.getCurrentSceneName()) {
                sceneInput.font = currentSceneInputFont
            } else {
                sceneInput.font = inputFont
            }
            sceneInputs[scene.name] = sceneInput
        }

        container.removeAll()
        container.layout = GridLayout(sceneLabels.size + 1, 2, 10, 5)
        container.add(JLabel("Scene"))
        container.add(JLabel("Duration (sec.)"))

        for (scene in Globals.scenes.values) {
            container.add(sceneLabels[scene.name])
            container.add(sceneInputs[scene.name])
        }
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
