package skacce.rs.kollago.gui.elements

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.GuiElement
import skacce.rs.kollago.gui.GuiScreen
import skacce.rs.kollago.input.GdxInputHandler
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.input.text.TextInputListener
import skacce.rs.kollago.input.text.TextInputProvider

class GuiTextInput(
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        private val placeholder: String,
        private val type: TextInputProvider.InputType,
        private val maxChars: Int,
        private val parent: GuiScreen
) : GuiElement, InputHandler {

    private val game: KollaGO = KollaGO.INSTANCE

    private val inputHandler: GdxInputHandler = game.inputHandler
    private val textRenderer: TextRenderer = game.textRenderer
    private val textInputProvider: TextInputProvider = game.textInputProvider

    var text = ""
    private var renderText = ""
    private val suffix = ""

    private var bounds: Rectangle? = null

    private var normalTexture: RepeatedNinePatch? = null

    var isVisible = true

    private class StaticTextInputHandler : TextInputListener {
        override fun textUpdated(text: String) {
            if (focusedElement != null) {
                focusedElement!!.updateText(text)
            }
        }
    }

    override fun create() {
        inputHandler.addInputHandler(this)

        normalTexture = RepeatedNinePatch("gui/text_input.png", "gui/text_input_repeat.png", width.toInt(), 47, 47, 50, 50)

        if (!registeredListener) {
            registeredListener = true
            textInputProvider.registerListener(StaticTextInputHandler())
        }

        bounds = Rectangle(x, y, width, height)
    }

    override fun destroy() {
        inputHandler.removeInputHandler(this)
    }

    override fun render(spriteBatch: SpriteBatch, delta: Float) {
        if (!isVisible) {
            return
        }

        bounds = Rectangle(x, y, width, height)

        normalTexture!!.draw(spriteBatch, x, y, width, height)

        renderText = if(type !== TextInputProvider.InputType.PASSWORD) text else "\u2022".repeat(text.length)

        if (text.isEmpty() && !placeholder.isEmpty() && focusedElement !== this) {
            textRenderer.drawCenteredText(placeholder, x + width / 2, y + height / 2 + 3f, 26, "Roboto", FontStyle.NORMAL, Color.LIGHT_GRAY)
        } else {
            textRenderer.drawCenteredText("$renderText $suffix", x + width / 2, y + height / 2 + 3f, 26, "Roboto", FontStyle.NORMAL, Color.WHITE)
        }
    }

    private fun updateText(text: String) {
        if (!isVisible) {
            return
        }

        this.text = text
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if (isVisible && bounds!!.contains(x, y)) {
            focusedElement = this
            textInputProvider.openTextInput(placeholder, text, type, maxChars)

            return true
        }

        if (parent.guiElements.stream().filter { it: GuiElement -> it is GuiTextInput }.noneMatch { it: GuiElement -> (it as GuiTextInput).bounds!!.contains(x, y) }) {
            textInputProvider.closeTextInput()
            focusedElement = null

            return true
        }

        return false
    }

    companion object {
        private var registeredListener = false
        private var focusedElement: GuiTextInput? = null
    }
}
