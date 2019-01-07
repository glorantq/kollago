package skacce.rs.kollago.graphics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.google.gson.Gson
import skacce.rs.kollago.screens.LoadingScreen

class TextureLoadingPerformer(private val assetManager: AssetManager) : LoadingScreen.LoadingPerformer {
    override val action: String = "Textúrák betöltése"
    override var loadingProgress: Int = 0
    override var done: Boolean = false

    init {
        val assetsFile: FileHandle = Gdx.files.internal("assets.json")

        val textureData: TextureFileData = Gson().fromJson(assetsFile.readString(), TextureFileData::class.java)

        textureData.textures.forEach {
            assetManager.load(it, Texture::class.java)
        }

        textureData.music.forEach {
            assetManager.load(it, Music::class.java)
        }

        textureData.sound.forEach {
            assetManager.load(it, Sound::class.java)
        }
    }

    override fun performLoading() {
        loadingProgress = (assetManager.progress * 100).toInt()

        if(assetManager.update()) {
            done = true
        }
    }
}