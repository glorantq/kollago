package skacce.rs.kollago.gui.elements

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.GuiElement
import skacce.rs.kollago.input.GdxInputHandler
import skacce.rs.kollago.input.InputHandler

import java.util.function.Consumer

class GuiButton(override val x: Float, override val y: Float, override val width: Float, override val height: Float, private val text: String, private val style: Style) : GuiElement, InputHandler {
    private lateinit var inputHandler: GdxInputHandler
    private lateinit var textRenderer: TextRenderer

    private val bounds: Rectangle = Rectangle(x, y, width, height)
    private var textSize: Vector2 = vec2()

    private lateinit var normalTexture: NinePatch
    private lateinit var hoverTexture: NinePatch

    lateinit var clickHandler: (longPress: Boolean) -> Unit
    private var touching: Boolean = false

    var visible: Boolean = true
    var enabled: Boolean = true
    var catchBackKey: Boolean = false

    override fun create() {
        val game: KollaGO = KollaGO.INSTANCE

        inputHandler = game.inputHandler
        inputHandler.addInputHandler(this)

        textRenderer = game.textRenderer
        textSize = textRenderer.getTextSize(text, style.fontName, style.fontStyle, style.fontSize)

        val textureManager: TextureManager = game.textureManager

        normalTexture = NinePatch(textureManager["gui/button_normal.png"], 40, 40, 40, 40)
        hoverTexture = NinePatch(textureManager["gui/button_hover.png"], 40, 40, 40, 40)
    }

    override fun destroy() {
        inputHandler.removeInputHandler(this)
    }

    override fun render(spriteBatch: SpriteBatch, delta: Float) {
        if (!visible) {
            return
        }

        bounds.set(x, y, width, height)

        if (touching || !enabled) {
            hoverTexture.draw(spriteBatch, x, y, width, height)
        } else {
            normalTexture.draw(spriteBatch, x, y, width, height)
        }

        textRenderer.drawCenteredText(text, x + width / 2, y + height / 2, style.fontSize, style.fontName, style.fontStyle, if(enabled) style.fontColor else style.disabledColor)

        if (catchBackKey && Gdx.input.isKeyJustPressed(Input.Keys.BACK) && visible && enabled) {
            clickHandler(false)
        }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if (!bounds.contains(x, y) || !::clickHandler.isInitialized || !visible || !enabled) {
            return false
        }

        clickHandler(false)

        return true
    }

    override fun longPress(x: Float, y: Float): Boolean {
        if (!bounds.contains(x, y) || !::clickHandler.isInitialized || !visible || !enabled) {
            return false
        }

        clickHandler(true)

        return true
    }

    override fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        if (!bounds.contains(x.toFloat(), y.toFloat()) || !visible || !enabled) {
            return false
        }

        touching = true

        return true
    }

    override fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        touching = false

        return true
    }

    data class Style(val fontName: String = "Roboto", val fontSize: Int = 30, val fontStyle: FontStyle = FontStyle.NORMAL, val fontColor: Color = Color.WHITE, val disabledColor: Color = Color.LIGHT_GRAY)
}
