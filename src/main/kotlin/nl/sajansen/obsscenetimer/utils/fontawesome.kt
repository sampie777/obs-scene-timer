package nl.sajansen.obsscenetimer.utils

import nl.sajansen.obsscenetimer.ApplicationInfo
import nl.sajansen.obsscenetimer.gui.utils.setDefaultRenderingHints
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JLabel
import javax.swing.Timer


private val faRegular = ApplicationInfo::class.java.getResourceAsStream("/nl/sajansen/obsscenetimer/fontawesome/fa-regular-400.ttf")
private val faSolid = ApplicationInfo::class.java.getResourceAsStream("/nl/sajansen/obsscenetimer/fontawesome/fa-solid-900.ttf")
val faRegularFont: Font = Font.createFont(Font.TRUETYPE_FONT, faRegular)
val faSolidFont: Font = Font.createFont(Font.TRUETYPE_FONT, faSolid)

open class FAIcon(
    icon: String,
    val fontSize: Float = 16f,
    solid: Boolean = true,
    val rotating: Boolean = false,
    rotationInterval: Int = 1000 / 30,
    var rotationStep: Double = 0.1,
) : JLabel(icon) {
    private var currentRotation: Double = 0.0

    init {
        font = if (solid) faSolidFont.deriveFont(fontSize) else faRegularFont.deriveFont(fontSize)

        if (rotating) {
            Timer(rotationInterval) { animationStep() }.start()
            Timer(1) { repaint() }.start()
        }
    }

    private fun animationStep() {
        currentRotation += rotationStep
        if (currentRotation >= 2 * Math.PI) {
            currentRotation = 0.0
        }
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        setDefaultRenderingHints(g2)
        g2.rotate(currentRotation, size.width.toDouble() / 2, size.height.toDouble() / 2)
        super.paintComponent(g)
    }
}