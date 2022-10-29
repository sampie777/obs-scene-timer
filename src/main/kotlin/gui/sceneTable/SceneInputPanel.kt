package gui.sceneTable

import objects.TScene
import java.awt.BorderLayout
import javax.swing.JPanel

class SceneInputPanel(private val scene: TScene) : JPanel() {
    val sceneInput = SceneInput(scene)

    init {
        initGUI()
    }

    private fun initGUI() {
        layout = BorderLayout(5, 5)

        add(sceneInput, BorderLayout.CENTER)
        add(SceneVideoReloadButton(scene), BorderLayout.LINE_END)
    }
}