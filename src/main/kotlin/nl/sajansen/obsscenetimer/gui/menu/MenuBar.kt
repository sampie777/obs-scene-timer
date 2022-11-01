package nl.sajansen.obsscenetimer.gui.menu

import javax.swing.JMenuBar

class MenuBar : JMenuBar() {
    init {
        add(ApplicationMenu())
        add(ToolsMenu())
    }
}