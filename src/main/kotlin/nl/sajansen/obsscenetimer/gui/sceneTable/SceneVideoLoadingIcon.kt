package nl.sajansen.obsscenetimer.gui.sceneTable

import nl.sajansen.obsscenetimer.utils.FAIcon
import javax.swing.BoxLayout
import javax.swing.JPanel

class SceneVideoLoadingIcon : JPanel() {
    init {
        background = null
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)

        toolTipText = "Calculating media length..."

        add(
            FAIcon(
                "\uf110", fontSize = 12f,
                rotating = true,
                rotationInterval = 1000 / 12,
                rotationStep = 2 * Math.PI / 8
            )
        )
    }
}