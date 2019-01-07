package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.gui.GuiScreen

open class MenuScreen : GuiScreen() {
    protected lateinit var viewport: Viewport

    private lateinit var backgroundTexture: Texture
    private lateinit var backgroundSize: Vector2

    private lateinit var logoTexture: Texture
    private lateinit var logoSize: Vector2

    override fun show() {
        val game: KollaGO = KollaGO.INSTANCE

        viewport = game.staticViewport

        val textureManager: TextureManager = game.textureManager
        logoTexture = textureManager["gui/logo.png"]
        backgroundTexture = textureManager["gui/background.png"]

        scaleBackground()
    }

    override fun draw(spriteBatch: SpriteBatch) {
        drawBackground(spriteBatch)
        drawLogo(spriteBatch)
    }

    protected fun drawBackground(spriteBatch: SpriteBatch) {
        spriteBatch.setColor(.6f, .6f, .7f, 1f)
        spriteBatch.draw(backgroundTexture, viewport.worldWidth / 2 - backgroundSize.x / 2, viewport.worldHeight / 2 - backgroundSize.y / 2, backgroundSize.x, backgroundSize.y)
        spriteBatch.setColor(1f, 1f, 1f, 1f)
    }

    private fun drawLogo(spriteBatch: SpriteBatch) {
        spriteBatch.draw(logoTexture, viewport.worldWidth / 2 - logoSize.x / 2, viewport.worldHeight - logoSize.y - 20f - KollaGO.SAFE_AREA_OFFSET, logoSize.x, logoSize.y)
    }

    override fun hide() {

    }

    private fun scaleBackground() {
        val widthRatio = viewport.worldWidth / backgroundTexture.width
        val heightRatio = viewport.worldHeight / backgroundTexture.height
        val ratio = Math.max(widthRatio, heightRatio)

        backgroundSize = Vector2(ratio * backgroundTexture.width, ratio * backgroundTexture.height)
    }

    private fun scaleLogo() {
        val widthRatio = 720f / logoTexture.width
        val heightRatio = 150f / logoTexture.height
        val ratio = Math.min(widthRatio, heightRatio)

        logoSize = Vector2(ratio * logoTexture.width, ratio * logoTexture.height)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        scaleBackground()
        scaleLogo()
    }
}
