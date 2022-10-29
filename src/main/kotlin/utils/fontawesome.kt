package utils

import ApplicationInfo
import java.awt.Font
import javax.swing.JLabel


private val faRegular = ApplicationInfo::class.java.getResourceAsStream("/fontawesome/fa-regular-400.ttf")
private val faSolid = ApplicationInfo::class.java.getResourceAsStream("/fontawesome/fa-solid-900.ttf")
val faRegularFont: Font = Font.createFont(Font.TRUETYPE_FONT, faRegular)
val faSolidFont: Font = Font.createFont(Font.TRUETYPE_FONT, faSolid)

class FAIcon(icon: String, size: Float = 16f, solid: Boolean = true) : JLabel(icon) {
    init {
        font = if (solid) faSolidFont.deriveFont(size) else faRegularFont.deriveFont(size)
    }
}