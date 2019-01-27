package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ktx.math.plus
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.utils.draw
import skacce.rs.kollago.utils.level
import skacce.rs.kollago.utils.levelProgress
import skacce.rs.kollago.utils.scaleToWidth

class HUDRenderer : InputHandler {
    private val game: KollaGO = KollaGO.INSTANCE

    private val playerPortraits: Array<Texture> = arrayOf(
            game.textureManager["portrait/0.png"],
            game.textureManager["portrait/1.png"]
    )

    private val levelProgress: Array<NinePatch> = arrayOf(
            NinePatch(game.textureManager["gui/level_progress_0.png"], 4, 4, 4, 4),
            NinePatch(game.textureManager["gui/level_progress_1.png"], 4, 4, 4, 4)
    )

    private val profileBg: Texture = game.textureManager["gui/profile_bg.png"]
    private val profileBgSize: Vector2 = profileBg.scaleToWidth(game.staticViewport.worldWidth / 2.5f)
    private val plaqueSize: Vector2 = vec2(profileBgSize.x, 298f * (profileBgSize.y / profileBg.height))

    private val avatarOverlay: Texture = game.textureManager["gui/avatar_overlay.png"]

    private val avatarSize: Float = 255f * (profileBgSize.x / profileBg.width)
    private val avatarPosition: Vector2 = vec2(30f * (profileBgSize.x / profileBg.width), 161f * (profileBgSize.y / profileBg.height)) + vec2(10f, 10f)

    private val avatarShader: ShaderProgram = ShaderProgram(Gdx.files.internal("shaders/profile_avatar.vert"), Gdx.files.internal("shaders/profile_avatar.frag"))

    private val profileBounds: Rectangle = Rectangle(10f, 10f, profileBgSize.x, profileBgSize.y)

    private val coinTexture: Texture = game.textureManager["gui/coin_1.png"]
    private val metalBackground: RepeatedNinePatch = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", (game.staticViewport.worldWidth / 2f).toInt(), 47, 47, 50, 50)

    private val coinBounds: Rectangle = Rectangle()

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()

    init {
        game.inputHandler.addInputHandler(this)
    }

    fun render() {
        val profile: ProfileData = game.networkManager.ownProfile

        profile@run {
            game.spriteBatch.shader = avatarShader

            avatarOverlay.bind(1)
            avatarShader.setUniformi("u_overlayTexture", 1)

            playerPortraits[profile.model.value].bind(0)
            avatarShader.setUniformi("u_texture", 0)

            game.spriteBatch.draw(playerPortraits[profile.model.value], avatarPosition, avatarSize)
            game.spriteBatch.shader = null

            game.spriteBatch.draw(profileBg, 10f, 10f, profileBgSize)

            game.textRenderer.drawCenteredText(profile.username, 10f + profileBgSize.x / 2, 10f + 25f, 30, "Hemi", FontStyle.NORMAL, Color.WHITE)

            val levelText: String = "Lv. ${profile.level().toInt()}"
            temp.set(game.textRenderer.getTextSize(levelText, "Hemi", FontStyle.NORMAL, 24))

            game.textRenderer.drawText(levelText, 10f + 15f, 10f + plaqueSize.y / 2 - temp.y * 2 - temp.y / 2, 24, "Hemi", FontStyle.NORMAL, Color.WHITE, true)

            temp.set(15f + temp.x + 20f, 10f + plaqueSize.y / 2 - temp.y * 2 - temp.y / 2 + levelProgress[0].texture.height.toFloat() / 2)
            temp2.set(profileBgSize.x - temp.x, levelProgress[0].texture.height.toFloat())

            levelProgress[0].draw(game.spriteBatch, temp.x, temp.y, temp2.x, temp2.y)

            val progressWidth: Float = temp2.x * profile.levelProgress()

            if (progressWidth > 0) {
                levelProgress[1].draw(game.spriteBatch, temp.x, temp.y, progressWidth, temp2.y)
            }
        }

        coins@run {
            val coinsText: String = profile.coins.toString()
            temp.set(game.textRenderer.getTextSize(coinsText, "Hemi", FontStyle.NORMAL, 30))

            val width: Float = temp.x + 20f + 10f + 48f + /* adjust */ 40f
            val height: Float = 48f + 20f + /* adjust */ 20f

            temp2.set(game.staticViewport.worldWidth - 10f - width, game.staticViewport.worldHeight - KollaGO.SAFE_AREA_OFFSET - height)

            val baseX: Float = temp2.x + width / 2 - (48f + temp.x + 5f) / 2
            val baseY: Float = temp2.y + height / 2 - 24f

            metalBackground.draw(game.spriteBatch, temp2.x, temp2.y, width, height)
            game.spriteBatch.draw(coinTexture, baseX, baseY, 48f, 48f)
            game.textRenderer.drawText(coinsText, baseX + 48f + 5f, baseY + 24f - temp.y, 30, "Hemi", FontStyle.NORMAL, Color.WHITE, true)

            coinBounds.set(temp2.x, temp2.y, width, height)
        }
    }

    fun containsCoordinates(x: Float, y: Float): Boolean = profileBounds.contains(x, y) || coinBounds.contains(x, y)

    fun dispose() {
        game.inputHandler.removeInputHandler(this)
    }
}