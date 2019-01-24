package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.network.protocol.BaseData
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.utils.level
import skacce.rs.kollago.utils.levelProgress
import skacce.rs.kollago.utils.levelXp
import skacce.rs.kollago.utils.xpToNextLevel

class PlayerBaseOverlay(private val base: BaseData) : ARWorld.Overlay {
    override val boundingBox: Rectangle = Rectangle()

    private val game: KollaGO = KollaGO.INSTANCE

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()

    private val viewport: Viewport = game.staticViewport

    private lateinit var nameBackground: RepeatedNinePatch
    private val background: NinePatch = NinePatch(game.textureManager["gui/menu_bg.png"], 20, 20, 20, 20)

    private val playerPortraits: Array<Texture> = arrayOf(
            game.textureManager["portrait/0.png"],
            game.textureManager["portrait/1.png"]
    )

    private val portraitOutline: Texture = game.textureManager["portrait/outline.png"]
    private var portraitSize: Float = 0f

    private val levelProgress: Array<NinePatch> = arrayOf(
            NinePatch(game.textureManager["gui/level_progress_0.png"], 4, 4, 4, 4),
            NinePatch(game.textureManager["gui/level_progress_1.png"], 4, 4, 4, 4)
    )

    override fun show() {
        val width: Float = viewport.worldWidth - 90f
        val height: Float = (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f

        portraitSize = width / 2 - 50f

        temp.set(game.textRenderer.getTextSize(base.ownerProfile!!.username, "Hemi",  FontStyle.NORMAL, 35))

        boundingBox.set(viewport.worldWidth / 2 - width / 2, (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f - height / 2, width, height)
        nameBackground = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", (boundingBox.x + boundingBox.width / 2 + 5f).toInt(), 47, 47, 50, 50)
    }

    override fun hide() {

    }

    override fun render() {
        val ownerProfile: ProfileData = base.ownerProfile!!

        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        temp.set(boundingBox.x + boundingBox.width / 2 - 5f - portraitSize, boundingBox.y + boundingBox.height - portraitSize)
        game.spriteBatch.draw(playerPortraits[ownerProfile.model.value], temp.x, temp.y, portraitSize, portraitSize)
        game.spriteBatch.draw(portraitOutline, temp.x - 10f, temp.y - 10f, portraitSize + 10f, portraitSize + 10f)

        temp2.set(boundingBox.x + boundingBox.width / 2 + 5f, temp.y + portraitSize / 2 + 10f)
        nameBackground.draw(game.spriteBatch, temp2.x, temp2.y, portraitSize + 10f, 80f)

        game.textRenderer.drawCenteredText(ownerProfile.username, temp2.x + (portraitSize + 10f) / 2, temp2.y + 40, 35, "Hemi", FontStyle.NORMAL, Color.WHITE)

        temp.set(temp2.x, temp.y + portraitSize / 2 - 10f - 32f)

        levelProgress[0].draw(game.spriteBatch, temp.x, temp.y, portraitSize + 10f, 32f)

        val progressWidth: Float = (portraitSize + 10f) * ownerProfile.levelProgress()

        if(progressWidth > 0) {
            levelProgress[1].draw(game.spriteBatch, temp.x, temp.y, progressWidth, 32f)
        }

        game.textRenderer.drawCenteredText("Lv. ${ownerProfile.level().toInt()}", temp.x + (portraitSize + 10f) / 2, temp.y + 16f, 24, "Hemi", FontStyle.NORMAL, Color.WHITE)

        val xpText: String = "${ownerProfile.xp - ownerProfile.levelXp(ownerProfile.level().toInt().toFloat())} XP"
        temp2.set(game.textRenderer.getTextSize(xpText, "Hemi", FontStyle.NORMAL, 24))
        game.textRenderer.drawText(xpText, temp.x + (portraitSize + 10f) / 2 - temp2.x / 2, temp.y - 10f, 24, "Hemi", FontStyle.NORMAL, Color.WHITE, false)
    }
}