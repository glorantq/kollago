package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.network.protocol.StopData

class RewardStopOverlay(private val stop: StopData) : ARWorld.Overlay {
    override val boundingBox: Rectangle = Rectangle()

    private val game: KollaGO = KollaGO.INSTANCE

    private val temp: Vector2 = vec2()
    private val viewport: Viewport = game.staticViewport

    private lateinit var nameBackground: RepeatedNinePatch
    private val background: NinePatch = NinePatch(game.textureManager["gui/menu_bg.png"], 20, 20, 20, 20)

    override fun show() {
        val width: Float = viewport.worldWidth - 90f
        val height: Float = (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f

        temp.set(game.textRenderer.getWrappedTextSize(stop.name, "Hemi", FontStyle.NORMAL, 35, width - 60f, Align.center))

        nameBackground = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", temp.x.toInt() + 20, 47, 47, 50, 50)
        boundingBox.set(viewport.worldWidth / 2 - width / 2, (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f - height / 2, width, height)
    }

    override fun hide() {

    }

    override fun render() {
        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        temp.set(game.textRenderer.getWrappedTextSize(stop.name, "Hemi", FontStyle.NORMAL, 35, boundingBox.width - 40f, Align.center))

        nameBackground.draw(game.spriteBatch, boundingBox.x + boundingBox.width / 2 - temp.x / 2f - 35f, boundingBox.y + boundingBox.height - 20f - (temp.y + 50f) / 2 - temp.y / 2, temp.x + 70f, temp.y + 50f)
        game.textRenderer.drawWrappedText(stop.name, boundingBox.x + 30f, boundingBox.y + boundingBox.height - 20f, 35, "Hemi", FontStyle.NORMAL, Color.WHITE, boundingBox.width - 60f, Align.center)
    }
}