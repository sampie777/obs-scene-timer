package nl.sajansen.obsscenetimer.gui.config.formcomponents

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.themes.Theme
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class SelectFormInput<T : Any>(
    override val key: String,
    private val labelText: String,
    private val values: List<T>
) : FormInput {

    private val logger = LoggerFactory.getLogger(SelectFormInput::class.java.name)

    private val selectBox = JComboBox<Any>()

    @Suppress("UNCHECKED_CAST")
    override fun component(): Component {
        val label = JLabel(labelText)
        label.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 12)

        val valuesArray = (values.toList() as ArrayList).toArray()
        selectBox.model = DefaultComboBoxModel(valuesArray)
        selectBox.selectedItem = values.find { it == Config.get(key) }
        selectBox.preferredSize = Dimension(100, 30)
        selectBox.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)

        val panel = JPanel()
        panel.layout = BorderLayout(10, 10)
        panel.add(label, BorderLayout.LINE_START)
        panel.add(selectBox, BorderLayout.LINE_END)
        return panel
    }

    override fun validate(): List<String> {
        val errors = ArrayList<String>()

        if (!values.contains(value())) {
            errors.add("Selected invalid value '${value()}' for '$labelText'")
        }

        return errors
    }

    override fun save() {
        Config.set(key, value().toString())
    }

    @Suppress("UNCHECKED_CAST")
    override fun value(): T {
        return selectBox.selectedItem as T
    }
}