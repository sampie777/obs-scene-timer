package nl.sajansen.obsscenetimer.gui.grouping

import nl.sajansen.obsscenetimer.gui.utils.DefaultDialogKeyDispatcher
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel

class GroupingFrame(private val parentFrame: JFrame?) : JDialog(parentFrame) {
    private val logger = LoggerFactory.getLogger(GroupingFrame::class.java.name)

    private val groupingMatrixPanel = GroupingMatrixPanel()

    companion object {
        fun create(parentFrame: JFrame?): GroupingFrame = GroupingFrame(parentFrame)

        fun createAndShow(parentFrame: JFrame?): GroupingFrame {
            val frame = create(parentFrame)
            frame.isVisible = true
            return frame
        }
    }

    init {
        KeyboardFocusManager
            .getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(DefaultDialogKeyDispatcher(this))

        createGui()
    }

    private fun createGui() {
        val mainPanel = JPanel(BorderLayout(10, 10))
        add(mainPanel)

        mainPanel.add(groupingMatrixPanel, BorderLayout.CENTER)
        mainPanel.add(GroupingActionPanel(this), BorderLayout.PAGE_END)

        title = "Group Settings"
        pack()
        setLocationRelativeTo(parentFrame)
        modalityType = ModalityType.APPLICATION_MODAL
    }

    fun rebuildGui() {
        groupingMatrixPanel.rebuildGui()
    }
}