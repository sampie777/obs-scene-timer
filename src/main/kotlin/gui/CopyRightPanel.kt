package gui

import java.awt.Color
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel

class CopyRightPanel : JPanel() {
    init {
        val label = JLabel("Samuel-Anton Jansen © 2020")
        label.font = Font("Dialog", Font.PLAIN, 10)
        label.foreground = Color(200, 200, 200)
        add(label)

        background = null
    }
}