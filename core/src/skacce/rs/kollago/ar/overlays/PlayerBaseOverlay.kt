package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.math.vec2
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.gui.elements.GuiButton
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.protocol.*
import skacce.rs.kollago.utils.*

class PlayerBaseOverlay(private val base: BaseData, private val vtmMap: VTMMap, private val showSettings: Boolean = false) : ARWorld.Overlay {
    private companion object {
        private val game: KollaGO = KollaGO.INSTANCE

        fun getLevelTexture(base: BaseData): Texture = game.textureManager["base/levels/${base.level}.png"]
    }

    override val boundingBox: Rectangle = Rectangle()

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()
    private val temp3: Vector2 = vec2()

    private val geoPoint: GeoPoint = base.coordinates!!.toGeoPoint()

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

    private val levelTexture: Texture = getLevelTexture(base)
    private val levelTextureSize: Vector2 = Vector2()

    private val guiButtons: MutableList<GuiButton> = ArrayList()

    private var attackButton: GuiButton? = null
    private var moveBaseButton: GuiButton? = null

    override fun show() {
        val width: Float = viewport.worldWidth - 90f

        levelTextureSize.set(levelTexture.scaleToWidth(width - 60f))
        portraitSize = width / 2 - 50f

        val height: Float = (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 1.3f

        temp.set(game.textRenderer.getTextSize(base.ownerProfile!!.username, "Hemi", FontStyle.NORMAL, 35))

        boundingBox.set(viewport.worldWidth / 2 - width / 2, (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f - height / 2, width, height)

        nameBackground = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", (boundingBox.x + boundingBox.width / 2 + 5f).toInt(), 47, 47, 50, 50)

        temp.set(boundingBox.x + 10f, boundingBox.y + 20f + KollaGO.SAFE_AREA_OFFSET / 2)
        temp2.set(boundingBox.width - 20f, 75f)

        if (showSettings) {
            moveBaseButton = GuiButton(temp.x, temp.y, temp2.x, temp2.y, "Bázis Áthelyezése", GuiButton.Style())

            moveBaseButton!!.clickHandler = clickHandler@{
                (game.screen as ARWorld).showOverlay(MoveBaseConfirmationOverlay(vtmMap))
            }

            guiButtons.add(moveBaseButton!!)
        } else {
            attackButton = GuiButton(temp.x, temp.y, temp2.x, temp2.y, "Valós", GuiButton.Style())
            attackButton!!.enabled = base.baseId != game.networkManager.ownBase.baseId

            attackButton!!.clickHandler = {
                (game.screen as ARWorld).showOverlay(AttackConfirmationOverlay(base, vtmMap))
            }

            guiButtons.add(attackButton!!)
        }

        guiButtons.forEach {
            it.create()
        }
    }

    override fun hide() {
        guiButtons.forEach {
            it.destroy()
        }
    }

    override fun render() {
        val ownerProfile: ProfileData = base.ownerProfile!!

        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        temp.set(boundingBox.x + boundingBox.width / 2 - 5f - portraitSize, boundingBox.y + boundingBox.height - portraitSize)
        temp3.set(temp)
        game.spriteBatch.draw(playerPortraits[ownerProfile.model.value], temp.x, temp.y, portraitSize, portraitSize)
        game.spriteBatch.draw(portraitOutline, temp.x - 10f, temp.y - 10f, portraitSize + 10f, portraitSize + 10f)

        temp2.set(boundingBox.x + boundingBox.width / 2 + 5f, temp.y + portraitSize / 2 + 10f)
        nameBackground.draw(game.spriteBatch, temp2.x, temp2.y, portraitSize + 10f, 80f)

        game.textRenderer.drawCenteredText(ownerProfile.username, temp2.x + (portraitSize + 10f) / 2, temp2.y + 40, 35, "Hemi", FontStyle.NORMAL, Color.WHITE)

        temp.set(temp2.x, temp.y + portraitSize / 2 - 10f - 32f)

        levelProgress[0].draw(game.spriteBatch, temp.x, temp.y, portraitSize + 10f, 32f)

        val progressWidth: Float = (portraitSize + 10f) * ownerProfile.levelProgress()

        if (progressWidth > 0) {
            levelProgress[1].draw(game.spriteBatch, temp.x, temp.y, progressWidth, 32f)
        }

        game.textRenderer.drawCenteredText("Lv. ${ownerProfile.level().toInt()}", temp.x + (portraitSize + 10f) / 2, temp.y + 16f, 24, "Hemi", FontStyle.NORMAL, Color.WHITE)

        val xpText: String = "${Math.max(0, ownerProfile.xp - ownerProfile.levelXp(ownerProfile.level().toInt().toFloat()))} XP"
        temp2.set(game.textRenderer.getTextSize(xpText, "Hemi", FontStyle.NORMAL, 24))
        game.textRenderer.drawText(xpText, temp.x + (portraitSize + 10f) / 2 - temp2.x / 2, temp.y - 10f, 24, "Hemi", FontStyle.NORMAL, Color.WHITE, false)

        temp2.set(boundingBox.x + boundingBox.width / 2 - levelTextureSize.x / 2, temp3.y - 40f - levelTextureSize.y)
        game.spriteBatch.draw(levelTexture, temp2, levelTextureSize)

        val distance: Float = geoPoint.sphericalDistance(vtmMap.getLocation()).toFloat()
        val timeout: Long = base.timeout - System.currentTimeMillis()

        val message: String = when {
            base.baseId == game.networkManager.ownBase.baseId -> "Nem támadhatod meg saját magad!"

            distance > 50f -> "Túl messze vagy"

            timeout > 0 -> {
                var seconds: Long = timeout / 1000L
                val minutes: Long = seconds / 60L

                seconds -= minutes * 60L

                "Ez a bázis %02d:%02d múlva lesz támadható".format(minutes, seconds)
            }

            game.networkManager.ownProfile.coins < 1000 -> "Nincs elég pénzed!"

            else -> "Támadás"
        }

        attackButton?.text = message
        attackButton?.enabled = distance <= 50 && timeout <= 0 && base.baseId != game.networkManager.ownBase.baseId && game.networkManager.ownProfile.coins >= 1000

        guiButtons.forEach {
            it.render(game.spriteBatch, Gdx.graphics.deltaTime)
        }
    }
}