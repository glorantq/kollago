package skacce.rs.kollago.gui.elements

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.GuiElement
import skacce.rs.kollago.input.GdxInputHandler
import skacce.rs.kollago.input.InputHandler
import java.util.function.Consumer

class GuiCheckBox(
        override val x: Float,
        override val y: Float, override var width: Float, override var height: Float, private val text: String, private val style: GuiButton.Style) : GuiElement, InputHandler {

    private val game: KollaGO = KollaGO.INSTANCE

    private val inputHandler: GdxInputHandler = game.inputHandler
    private val textRenderer: TextRenderer = game.textRenderer

    private val checkBoxBounds: Rectangle = Rectangle(x.toInt().toFloat(), y.toInt().toFloat(), width.toInt().toFloat(), height.toInt().toFloat())

    private lateinit var normalTexture: NinePatch
    private lateinit var checkmarkTexture: Texture

    private var checked: Boolean = false

    private lateinit var stateChangeHandler: Consumer<Boolean>

    init {
        val textSize: Vector2 = textRenderer.getTextSize(text, style.fontName, style.fontStyle, style.fontSize)
        val totalBounds: Rectangle = Rectangle(x.toInt().toFloat(), y.toInt().toFloat(), (width + 5f + textSize.x).toInt().toFloat(), height.toInt().toFloat())

        width = totalBounds.getWidth()
        height = totalBounds.getHeight()
    }

    override fun create() {
        val textureManager: TextureManager = game.textureManager

        normalTexture = NinePatch(textureManager["gui/button_normal.png"], 40, 40, 40, 40)
        checkmarkTexture = textureManager["gui/check.png"]

        inputHandler.addInputHandler(this)
    }

    override fun render(spriteBatch: SpriteBatch, delta: Float) {
        checkBoxBounds.setX(x)
        checkBoxBounds.setY(y)

        normalTexture.draw(spriteBatch, x, y, checkBoxBounds.width, checkBoxBounds.height)

        if (checked) {
            val checkMarkWidth: Float = checkBoxBounds.getWidth() / 1.5f
            val checkMarkHeight: Float = checkBoxBounds.getHeight() / 1.5f

            spriteBatch.draw(checkmarkTexture, x + checkBoxBounds.getWidth() / 2 - checkMarkWidth / 2, y + checkBoxBounds.getHeight() / 2 - checkMarkHeight / 2, checkMarkWidth, checkMarkHeight)
        }

        textRenderer.drawCenteredText(text, x + 10f + (width + checkBoxBounds.getWidth()).toInt() / 2f, y + height / 2, style.fontSize, style.fontName, style.fontStyle, style.fontColor)
    }

    override fun destroy() {
        inputHandler.removeInputHandler(this)
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if (checkBoxBounds.contains(x, y)) {
            checked = !checked

            if (::stateChangeHandler.isInitialized) {
                stateChangeHandler.accept(checked)
            }

            return true
        }

        return false
    }
}
