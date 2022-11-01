package nl.sajansen.obsscenetimer.gui.grouping


import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.obs.OBSState
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.util.logging.Logger
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class GroupingActionPanel(private val frame: GroupingFrame) : JPanel() {
    private val logger = Logger.getLogger(GroupingActionPanel::class.java.name)

    init {
        createGui()
    }

    private fun createGui() {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        border = EmptyBorder(0, 10, 10, 10)

        val clearAllButton = JButton("Clear all")
        clearAllButton.addActionListener { clearAllGrouping() }
        clearAllButton.mnemonic = KeyEvent.VK_A

        val doneButton = JButton("Done")
        doneButton.addActionListener { saveConfigAndClose() }
        doneButton.mnemonic = KeyEvent.VK_D
        frame.rootPane.defaultButton = doneButton

        add(Box.createHorizontalGlue())
        add(clearAllButton)
        add(Box.createRigidArea(Dimension(10, 0)))
        add(doneButton)
    }

    private fun saveConfigAndClose() {
        logger.info("Closing grouping configuration window")

        Config.save()

        frame.dispose()
    }

    private fun clearAllGrouping() {
        logger.info("Clear all grouping")

        OBSState.scenes.forEach {
            it.removeFromAllGroups()
        }

        GUI.refreshGroups()
        frame.rebuildGui()
    }
}