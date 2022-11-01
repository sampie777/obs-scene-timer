package nl.sajansen.obsscenetimer.gui

import nl.sajansen.obsscenetimer.gui.utils.createGraphics
import nl.sajansen.obsscenetimer.gui.utils.drawImageInYCenter
import nl.sajansen.obsscenetimer.gui.utils.setDefaultRenderingHints
import nl.sajansen.obsscenetimer.objects.OBSSceneTimer
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

class TimerProgressBarPanel : JPanel() {

    private val paintMargin = 5
    private val barHeight = 30
    private val cursorHeight = 20

    init {
        preferredSize = Dimension(300, 40)
        maximumSize = Dimension(800, 40)
        background = null
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (OBSSceneTimer.getMaxTimerValue() == 0L) {
            return
        }

        val g2 = g as Graphics2D
        setDefaultRenderingHints(g2)

        drawImageInYCenter(g2, height, 0, drawEmptyBar())

        val cursorPositionPercentage: Double
        if (OBSSceneTimer.getValue() >= OBSSceneTimer.getMaxTimerValue()) {
            cursorPositionPercentage = 1.0
        } else {
            cursorPositionPercentage = OBSSceneTimer.getValue().toDouble() / OBSSceneTimer.getMaxTimerValue()
        }
        val cursorPositionX = cursorPositionPercentage * (width - 2.0 * paintMargin)

        val progressCursor = drawProgressCursor()
        drawImageInYCenter(
            g2,
            height,
            (paintMargin + cursorPositionX + -1 * progressCursor.width / 2.0).toInt(),
            progressCursor
        )
    }

    private fun drawProgressCursor(): BufferedImage {
        val (bufferedImage, g2: Graphics2D) = createGraphics(
            cursorHeight + 2 * paintMargin,
            cursorHeight + 2 * paintMargin
        )
        g2.stroke = BasicStroke(3F)

        val progressBal = Ellipse2D.Double(paintMargin.toDouble(), paintMargin.toDouble(), cursorHeight.toDouble(), cursorHeight.toDouble())

        g2.color = Color.WHITE
        g2.fill(progressBal)
        g2.color = Color.BLACK
        g2.draw(progressBal)

        g2.dispose()
        return bufferedImage
    }

    private fun drawEmptyBar(): BufferedImage {
        val (bufferedImage, g2: Graphics2D) = createGraphics(width, barHeight)
        g2.stroke = BasicStroke(3F)

        g2.color = Color.BLACK
        g2.drawLine(paintMargin, 0, paintMargin, bufferedImage.height)
        g2.drawLine(paintMargin, bufferedImage.height / 2, bufferedImage.width - paintMargin, bufferedImage.height / 2)
        g2.drawLine(bufferedImage.width - paintMargin, 0, bufferedImage.width - paintMargin, bufferedImage.height)

        g2.dispose()
        return bufferedImage
    }
}