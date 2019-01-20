package skacce.rs.kollago.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.collections.gdxArrayOf
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.gui.GuiScreen

open class MenuScreen : GuiScreen() {
    private companion object {
        private var animationStateTime: Float = 0f
        private var logo: Animation<TextureRegion>? = null
    }

    protected var viewport: Viewport

    private var backgroundTexture: Texture
    private lateinit var backgroundSize: Vector2

    private var logoTexture: TextureRegion
    private lateinit var logoSize: Vector2

    init {
        val game: KollaGO = KollaGO.INSTANCE

        viewport = game.staticViewport

        val textureManager: TextureManager = game.textureManager

        if(logo == null) {
            val frames: MutableList<TextureRegion> = arrayListOf()
            TextureRegion.split(textureManager["gui/logo.png"], 654, 99).forEach {
                it.forEach {
                    frames.add(it)
                }
            }

            logo = Animation(1f / 60f, gdxArrayOf(*frames.subList(0, frames.size - 5).toTypedArray()), Animation.PlayMode.LOOP)
        }

        logoTexture = logo!!.getKeyFrame(animationStateTime)
        backgroundTexture = textureManager["gui/background.png"]

        scaleBackground()
        scaleLogo()
    }

    override fun show() {

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

        animationStateTime += Gdx.graphics.deltaTime
        logoTexture = logo!!.getKeyFrame(animationStateTime)
    }

    private fun scaleBackground() {
        val widthRatio = viewport.worldWidth / backgroundTexture.width
        val heightRatio = viewport.worldHeight / backgroundTexture.height
        val ratio = Math.max(widthRatio, heightRatio)

        backgroundSize = Vector2(ratio * backgroundTexture.width, ratio * backgroundTexture.height)
    }

    private fun scaleLogo() {
        val widthRatio = 720f / logoTexture.regionWidth
        val heightRatio = 150f / logoTexture.regionHeight
        val ratio = Math.min(widthRatio, heightRatio)

        logoSize = Vector2(ratio * logoTexture.regionWidth, ratio * logoTexture.regionHeight)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        scaleBackground()
        scaleLogo()
    }
}
