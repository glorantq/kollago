package skacce.rs.kollago.graphics

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import skacce.rs.kollago.KollaGO

class RepeatedNinePatch(baseTextureName: String, repeatTextureName: String, width: Int, left: Int, right: Int, top: Int, bottom: Int) {
    private val baseTexture: NinePatch
    private val repeatTexture: NinePatch

    init {
        val textureManager: TextureManager = KollaGO.INSTANCE.textureManager

        baseTexture = NinePatch(textureManager[baseTextureName], left, right, top, bottom)

        val repeatTexture0: Texture = KollaGO.INSTANCE.textureManager[repeatTextureName]
        repeatTexture0.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge)
        repeatTexture =  NinePatch(TextureRegion(repeatTexture0, width - baseTexture.leftWidth.toInt() - baseTexture.rightWidth.toInt(), repeatTexture0.height), 10, 10, baseTexture.topHeight.toInt(), baseTexture.bottomHeight.toInt())
    }

    fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, width: Float, height: Float) {
        repeatTexture.draw(spriteBatch, x + baseTexture.leftWidth, y, width - baseTexture.rightWidth - baseTexture.leftWidth, height)
        baseTexture.draw(spriteBatch, x, y, width, height)
    }
}