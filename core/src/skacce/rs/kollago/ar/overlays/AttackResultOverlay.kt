package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.Gdx
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
import skacce.rs.kollago.gui.elements.GuiButton

class AttackResultOverlay(private val coinsDelta: Long, private val xpDelta: Long, private val success: Boolean) : ARWorld.Overlay {
    private val game: KollaGO = KollaGO.INSTANCE

    override val boundingBox: Rectangle = Rectangle()

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()

    private val viewport: Viewport = game.staticViewport

    private lateinit var textBackground: RepeatedNinePatch
    private val background: NinePatch = NinePatch(game.textureManager["gui/menu_bg.png"], 20, 20, 20, 20)

    private val headerText: String = if(success) "Győzelem!" else "Majd legközelebb"

    private lateinit var backButton: GuiButton

    override fun show() {
        val width: Float = viewport.worldWidth - 90f
        val height: Float = (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 3f

        temp.set(game.textRenderer.getWrappedTextSize(headerText, "Hemi", FontStyle.NORMAL, 30, width - 60f, Align.center))

        textBackground = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", (width - 60f).toInt(), 47, 47, 50, 50)

        boundingBox.set(viewport.worldWidth / 2 - width / 2, (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f - height / 2, width, height)

        temp.set(boundingBox.width / 3 - 40f, 75f)
        backButton = GuiButton(boundingBox.x + boundingBox.width - 20f - temp.x, boundingBox.y + 20f + KollaGO.SAFE_AREA_OFFSET / 2, temp.x, temp.y, "OK", GuiButton.Style())

        backButton.create()

        backButton.clickHandler = {
            (game.screen as ARWorld).closeOverlay(force = true)
        }
    }

    override fun hide() {
        backButton.destroy()
    }

    override fun render() {
        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        temp.set(game.textRenderer.getWrappedTextSize(headerText, "Hemi", FontStyle.NORMAL, 30, boundingBox.width - 80f, Align.center))
        temp2.set(boundingBox.x + 30f, boundingBox.y + boundingBox.height - temp.y - 40f)
        temp.set(boundingBox.width - 60f, temp.y + 60f)

        textBackground.draw(game.spriteBatch, temp2.x, temp2.y, temp.x, temp.y)
        game.textRenderer.drawWrappedText(headerText, temp2.x + 20f, temp2.y + temp.y - 30f, 30, "Hemi", FontStyle.NORMAL, Color.WHITE, temp.x - 40f, Align.center)

        temp2.set(temp2.x, boundingBox.y + boundingBox.height / 2 + KollaGO.SAFE_AREA_OFFSET / 2)
        if(success) {
            renderSuccess()
        } else {
            renderFailure()
        }

        backButton.render(game.spriteBatch, Gdx.graphics.deltaTime)
    }

    private fun renderSuccess() {
        val xpText: String = "XP: $xpDelta"
        val coinsText: String = "Nyereség: $coinsDelta Kolla Coin"

        temp.set(game.textRenderer.getTextSize(xpText, "Hemi", FontStyle.NORMAL, 36))

        game.textRenderer.drawText(xpText, temp2.x, temp2.y + 10f, 36, "Hemi", FontStyle.NORMAL, Color.WHITE, true)
        game.textRenderer.drawText(coinsText, temp2.x, temp2.y - 10f, 36, "Hemi", FontStyle.NORMAL, Color.WHITE, false)
    }

    private fun renderFailure() {
        val lossText: String = "Veszteség: ${Math.abs(coinsDelta)} Kolla Coin"

        temp.set(game.textRenderer.getTextSize(lossText, "Hemi", FontStyle.NORMAL, 36))

        game.textRenderer.drawText(lossText, temp2.x, temp2.y + temp.y / 2, 36, "Hemi", FontStyle.NORMAL, Color.WHITE, false)
    }
}