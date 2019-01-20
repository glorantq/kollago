package skacce.rs.kollago.ar

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Vector2
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.utils.level
import skacce.rs.kollago.utils.levelProgress

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

    private val profileBg: NinePatch = NinePatch(game.textureManager["gui/profile_bg.png"], 45, 45, 45, 45)

    init {
        game.inputHandler.addInputHandler(this)
    }

    fun render() {
        val profile: ProfileData = game.networkManager.ownProfile
        val lvSize: Vector2 = game.textRenderer.getTextSize("lv", "Roboto", FontStyle.NORMAL, 24)
        val numSize: Vector2 = game.textRenderer.getTextSize("${profile.level().toInt()}", "Roboto", FontStyle.BOLD, 30)
        val nameSize: Vector2 = game.textRenderer.getTextSize(profile.username, "Roboto", FontStyle.BOLD, 30)

        var progressWidth: Float = 160f
        val progressPosition: Vector2 = vec2(10f + lvSize.x + numSize.x + 15f, 40f + numSize.y / 2f - 8f)

        val profileHeight: Float = 10f + nameSize.y + 5f + numSize.y + 40f
        profileBg.draw(game.spriteBatch, 0f, 0f, lvSize.x + numSize.x + progressWidth + 10f + 5f + 40f, profileHeight)

        game.spriteBatch.draw(playerPortraits[profile.model.value], 0f, profileHeight / 2, 64f, 64f, 128f, 128f, 1f, 1f, 0f, 0, 0, 512, 512, true, false)

        game.textRenderer.drawText(profile.username, 10f, 10f, 30, "Roboto", FontStyle.BOLD, Color.WHITE, true)
        game.textRenderer.drawText("lv", 10f, 10f + nameSize.y + 5f, 24, "Roboto", FontStyle.NORMAL, Color.WHITE, true)
        game.textRenderer.drawText("${profile.level().toInt()}", 10f + lvSize.x + 5f, 10f + nameSize.y + 5f, 30, "Roboto", FontStyle.BOLD, Color.WHITE, true)

        levelProgress[0].draw(game.spriteBatch, progressPosition.x, progressPosition.y, progressWidth, 16f)

        progressWidth *= profile.levelProgress()
        if(progressWidth >= 0.05) {
            levelProgress[1].draw(game.spriteBatch, progressPosition.x, progressPosition.y, progressWidth, 16f)
        }
    }

    fun dispose() {
        game.inputHandler.removeInputHandler(this)
    }
}