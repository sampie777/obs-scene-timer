package gui.grouping

import gui.utils.DefaultDialogKeyDispatcher
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.util.logging.Logger
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel

class GroupingFrame(private val parentFrame: JFrame?) : JDialog(parentFrame) {
    private val logger = Logger.getLogger(GroupingFrame::class.java.name)

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

    fun saveAll(): Boolean {
        return groupingMatrixPanel.saveAll()
    }
}