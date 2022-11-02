package nl.sajansen.obsscenetimer.gui.config.formcomponents

import nl.sajansen.obsscenetimer.config.Config
import nl.sajansen.obsscenetimer.themes.Theme
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class NumberFormInput<T : Number>(
    override val key: String,
    private val labelText: String,
    private val min: T?,
    private val max: T?,
    private val onSave: ((newValue: T) -> Unit)? = null
) : FormInput {
    private val logger = LoggerFactory.getLogger(NumberFormInput::class.java.name)

    private val input = JSpinner()

    @Suppress("UNCHECKED_CAST")
    override fun component(): Component {
        val configValue: T? = Config.get(key) as? T

        val label = JLabel("<html>${labelText.replace("\n", "<br/>&nbsp;")}</html>")
        label.font = Font(Theme.get.FONT_FAMILY, Font.PLAIN, 12)

        input.model = SpinnerNumberModel(configValue, min as? Comparable<T>, max as? Comparable<T>, 1)
        input.preferredSize = Dimension(100, 20)
        input.border = BorderFactory.createLineBorder(Theme.get.BORDER_COLOR)

        val panel = JPanel()
        panel.layout = BorderLayout(10, 10)
        panel.add(label, BorderLayout.LINE_START)
        panel.add(input, BorderLayout.LINE_END)
        return panel
    }

    override fun validate(): List<String> {
        val errors = ArrayList<String>()

        if (min != null && value().toDouble() < min.toDouble()) {
            errors.add("Value for '$labelText' is to small")
        }
        if (max != null && value().toDouble() > max.toDouble()) {
            errors.add("Value for '$labelText' is to large")
        }

        return errors
    }

    override fun save() {
        onSave?.invoke(value())

        Config.set(key, value())
    }

    @Suppress("UNCHECKED_CAST")
    override fun value(): T {
        return input.value as T
    }
}