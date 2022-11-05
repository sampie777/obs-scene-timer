package nl.sajansen.obsscenetimer.gui.grouping

import nl.sajansen.obsscenetimer.GUI
import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.gui.Refreshable
import nl.sajansen.obsscenetimer.objects.TScene
import nl.sajansen.obsscenetimer.obs.OBSState
import nl.sajansen.obsscenetimer.themes.Theme
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.math.min

class GroupingMatrixPanel : JPanel(), Refreshable {
    private val logger = LoggerFactory.getLogger(GroupingMatrixPanel::class.java.name)

    private val groupAmount = min(OBSState.scenes.size - 1, Config.maxGroups)
    private val tableHeadFont = Font(Theme.get.FONT_FAMILY, Font.BOLD, 12)
    private val tableContentFont = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 12)
    private val matrixVerticalSpacing: Int = 0
    private val matrixHorizontalSpacing: Int = 5

    init {
        createGui()

        GUI.register(this)
    }

    override fun removeNotify() {
        super.removeNotify()
        GUI.unregister(this)
    }

    fun rebuildGui() {
        logger.info("Rebuilding matrix panel")
        EventQueue.invokeLater {
            removeAll()

            createGui()

            revalidate()
            repaint()
            logger.info("GUI rebuild done")
        }
    }

    fun createGui() {
        layout = BorderLayout()
        border = EmptyBorder(10, 10, 10, 10)

        val scenesPanel = createScenesPanel()
        val checkboxMatrixPanel = createCheckboxMatrixPanel()

        val mainPanel = JPanel(BorderLayout(10, 10))
        mainPanel.add(scenesPanel, BorderLayout.LINE_START)
        mainPanel.add(checkboxMatrixPanel, BorderLayout.CENTER)

        val scrollPanelInnerPanel = JPanel(BorderLayout())
        scrollPanelInnerPanel.add(mainPanel, BorderLayout.PAGE_START)
        val scrollPanel = JScrollPane(scrollPanelInnerPanel)
        scrollPanel.border = null
        add(scrollPanel, BorderLayout.CENTER)
    }

    private fun createScenesPanel(): JPanel {
        val panel = JPanel()
        panel.layout = GridLayout(0, 1, matrixHorizontalSpacing, matrixVerticalSpacing)

        val sceneHeadLabel = JLabel("Scene")
        sceneHeadLabel.font = tableHeadFont
        panel.add(sceneHeadLabel)

        OBSState.scenes.forEach { scene ->
            val sceneLabel = JLabel(scene.name)
            sceneLabel.font = tableContentFont
            panel.add(sceneLabel)
        }

        return panel
    }

    private fun createCheckboxMatrixPanel(): JPanel {
        val panel = JPanel()
        panel.layout = GridLayout(1 + OBSState.scenes.size, groupAmount, matrixHorizontalSpacing, matrixVerticalSpacing)

        addMatrixTableHead(panel)
        addCheckboxMatrix(panel)

        return panel
    }

    private fun addMatrixTableHead(panel: JPanel) {
        (1 until (groupAmount + 1)).forEach {
            val groupLabel = JLabel("$it")
            groupLabel.font = tableHeadFont
            groupLabel.horizontalAlignment = SwingConstants.CENTER
            panel.add(groupLabel)
        }
    }

    private fun addCheckboxMatrix(panel: JPanel) {
        OBSState.scenes.forEach { scene ->
            (1 until (groupAmount + 1)).forEach { groupNumber ->
                val checkbox = JCheckBox()
                checkbox.toolTipText = "Assign '${scene.name}' to group $groupNumber"
                checkbox.addActionListener(GroupCheckboxActionListener(scene, groupNumber))
                checkbox.isSelected = scene.isInGroup(groupNumber)

                panel.add(checkbox)
            }
        }
    }

    override fun refreshScenes() {
        rebuildGui()
    }
}

class GroupCheckboxActionListener(private val scene: TScene, private val groupNumber: Int) : ActionListener {
    private val logger = LoggerFactory.getLogger(GroupCheckboxActionListener::class.java.name)

    override fun actionPerformed(e: ActionEvent) {
        if (scene.isInGroup(groupNumber)) {
            logger.info("Removing scene ${scene.name} from group $groupNumber")
            scene.removeFromGroup(groupNumber)
        } else {
            logger.info("Adding scene ${scene.name} to group $groupNumber")
            scene.addToGroup(groupNumber)
        }

        GUI.refreshGroups()
    }
}