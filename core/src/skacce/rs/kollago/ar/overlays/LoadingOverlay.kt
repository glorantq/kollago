package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.collections.gdxArrayOf
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld

class LoadingOverlay() : ARWorld.Overlay {
    private companion object {
        private val animationTexture: Texture = KollaGO.INSTANCE.textureManager["gui/loading_circle.png"]
        private val loadingAnimation: Animation<TextureRegion>

        init {
            val frames: MutableList<TextureRegion> = arrayListOf()

            TextureRegion.split(animationTexture, 200, 200).forEach {
                it.forEach {
                    frames.add(it)
                }
            }

            loadingAnimation = Animation(1f / 30f, gdxArrayOf(*frames.toTypedArray()), Animation.PlayMode.LOOP)
        }

        private const val size: Float = 256f

        private var stateTime: Float = 0f
    }

    private val game: KollaGO = KollaGO.INSTANCE
    private val viewport: Viewport = game.staticViewport

    override val boundingBox: Rectangle = Rectangle(viewport.worldWidth / 2 - (size + 80f) / 2, (viewport.worldHeight / 2 - KollaGO.SAFE_AREA_OFFSET) - (size + 80f) / 2, size + 80f, size + 80f)

    private val background: NinePatch = NinePatch(game.textureManager["gui/menu_bg.png"], 20, 20, 20, 20)

    override fun show() {
        stateTime = 0f
    }

    override fun hide() {

    }

    override fun render() {
        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        game.spriteBatch.draw(loadingAnimation.getKeyFrame(stateTime), boundingBox.x + 40f, boundingBox.y + 40f + KollaGO.SAFE_AREA_OFFSET, size, size)
        stateTime += Gdx.graphics.deltaTime
    }
}