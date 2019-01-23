package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import ktx.math.plus
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.utils.draw
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

    private val avatarOverlay: Texture = game.textureManager["gui/avatar_overlay.png"]

    private val avatarSize: Float = 255f * (profileBgSize.x / profileBg.width)
    private val avatarPosition: Vector2 = vec2(30f * (profileBgSize.x / profileBg.width), 161f * (profileBgSize.y / profileBg.height)) + vec2(10f, 10f)

    private val avatarShader: ShaderProgram = ShaderProgram(Gdx.files.internal("shaders/profile_avatar.vert"), Gdx.files.internal("shaders/profile_avatar.frag"))

    init {
        game.inputHandler.addInputHandler(this)
    }

    fun render() {
        val profile: ProfileData = game.networkManager.ownProfile

        game.spriteBatch.shader = avatarShader

        avatarOverlay.bind(1)
        avatarShader.setUniformi("u_overlayTexture", 1)

        playerPortraits[profile.model.value].bind(0)
        avatarShader.setUniformi("u_texture", 0)

        game.spriteBatch.draw(playerPortraits[profile.model.value], avatarPosition, avatarSize)
        game.spriteBatch.shader = null

        game.spriteBatch.draw(profileBg, 10f, 10f, profileBgSize)
    }

    fun dispose() {
        game.inputHandler.removeInputHandler(this)
    }
}