import gui.MainFrame
import java.awt.EventQueue
import java.util.logging.Logger

fun main(args: Array<String>) {
    val logger = Logger.getLogger("Application")
    logger.info("Starting application")

    EventQueue.invokeLater {
        MainFrame()
    }

    OBSClient()
}