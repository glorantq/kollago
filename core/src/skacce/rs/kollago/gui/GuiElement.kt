package skacce.rs.kollago.gui

import com.badlogic.gdx.graphics.g2d.SpriteBatch

interface GuiElement {
    val width: Float
    val height: Float
    val x: Float
    val y: Float

    fun create()
    fun destroy()
    fun render(spriteBatch: SpriteBatch, delta: Float)
}
