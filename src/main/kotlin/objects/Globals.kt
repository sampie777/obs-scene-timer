package objects

import gui.MainFrame
import net.twasi.obsremotejava.OBSRemoteController

object Globals {
    lateinit var mainFrame: MainFrame
    lateinit var obsController: OBSRemoteController
    val scenes: HashMap<String, TScene> = HashMap()
}