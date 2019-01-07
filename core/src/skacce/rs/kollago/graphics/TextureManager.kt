package skacce.rs.kollago.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import org.slf4j.LoggerFactory

class TextureManager (private val assetManager: AssetManager) {
    private val defaultTexture: Texture

    init {
        val defaultTexturePixmap: Pixmap = Pixmap(64, 64, Pixmap.Format.RGB888)

        defaultTexturePixmap.setColor(0f, 0f, 0f, 1f)
        defaultTexturePixmap.fillRectangle(0, 0, 64, 64)

        defaultTexturePixmap.setColor(1f, 0f, 0f, 1f)
        defaultTexturePixmap.fillRectangle(0, 0, 32, 32)
        defaultTexturePixmap.fillRectangle(32, 32, 32, 32)

        this.defaultTexture = Texture(defaultTexturePixmap)
    }

    fun getTexture(name: String): Texture {
        val texture = getTexture0(name)

        return texture
    }

    operator fun get(key: String): Texture = getTexture(key)

    private fun getTexture0(name: String): Texture {
        if (assetManager.isLoaded(name)) {
            return assetManager.get(name, Texture::class.java)
        }

        if (Gdx.files.internal(name).exists()) {
            Gdx.app.log("TextureManager", "Loading texture $name on-demand!")
            return Texture(name)
        }

        Gdx.app.error("TextureManager", "Texture $name not found!")
        return defaultTexture
    }
}
