package skacce.rs.kollago.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import skacce.rs.kollago.KollaGO
import java.util.*

abstract class GuiScreen : Screen {
    var guiElements: MutableList<GuiElement> = ArrayList()

    override fun render(delta: Float) {
        draw(KollaGO.INSTANCE.spriteBatch)
    }

    protected fun createElement(element: GuiElement) {
        element.create()
        guiElements.add(element)
    }

    protected fun drawElements(spriteBatch: SpriteBatch) {
        guiElements.forEach { it -> it.render(spriteBatch, Gdx.graphics.deltaTime) }
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    protected abstract fun draw(spriteBatch: SpriteBatch)

    override fun dispose() {
        guiElements.forEach { it.destroy() }
    }

    override fun hide() {
        dispose()
    }
}
