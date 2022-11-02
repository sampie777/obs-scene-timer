package nl.sajansen.obsscenetimer.gui.config.formcomponents

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.themes.Theme
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class StringFormInput(
    override val key: String,
    private val labelText: String,
    private val allowEmpty: Boolean,
    private val toolTipText: String = "",
    private val onSave: ((newValue: String) -> Unit)? = null
) : FormInput {
    private val logger = LoggerFactory.getLogger(StringFormInput::class.java.name)

    private val input = JTextField()

    override fun component(): Component {
        val configValue: String? = Config.get(key) as? String

        val label = JLabel(labelText)
        label.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 12)
        label.toolTipText = toolTipText

        input.text = configValue
        input.preferredSize = Dimension(100, 20)
        input.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)
        input.toolTipText = toolTipText

        val panel = JPanel()
        panel.layout = BorderLayout(10, 10)
        panel.toolTipText = toolTipText
        panel.add(label, BorderLayout.LINE_START)
        panel.add(input, BorderLayout.CENTER)
        return panel
    }

    override fun validate(): List<String> {
        val errors = ArrayList<String>()

        if (!allowEmpty && value().isEmpty()) {
            errors.add("Value for '$labelText' may not be empty")
        }

        return errors
    }

    override fun save() {
        onSave?.invoke(value())

        Config.set(key, value())
    }

    override fun value(): String {
        return input.text as String
    }
}