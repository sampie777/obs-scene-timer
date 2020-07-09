package gui.mainFrame

import GUI
import exitApplication
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent


class MainFrameWindowAdapter(private val frame: MainFrame) : WindowAdapter() {
    override fun windowClosing(winEvt: WindowEvent) {
        frame.saveWindowPosition()
        GUI.windowClosing(frame)
        exitApplication()
    }
}