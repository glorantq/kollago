package skacce.rs.kollago.graphics.text

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class Font(val name: String, normalPath: String?, italicPath: String?, boldPath: String?) {
    private val normalHandle: FileHandle? = if (normalPath == null) null else Gdx.files.internal(normalPath)
    private val italicHandle: FileHandle? = if (italicPath == null) null else Gdx.files.internal(italicPath)
    private val boldHandle: FileHandle? = if (boldPath == null) null else Gdx.files.internal(boldPath)

    internal fun getFont(style: FontStyle, size: Int): BitmapFont {
        val handle = getHandle(style)
        if (handle == null || !handle.exists()) {
            throw UnsupportedOperationException("This font doesn't support the ${style.name} style!")
        }

        val generator = FreeTypeFontGenerator(handle)

        val freeTypeFontParameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        freeTypeFontParameter.size = size
        freeTypeFontParameter.characters = CHARSET
        freeTypeFontParameter.color = Color.WHITE
        freeTypeFontParameter.minFilter = Texture.TextureFilter.Linear
        freeTypeFontParameter.magFilter = Texture.TextureFilter.Linear

        val font = generator.generateFont(freeTypeFontParameter)

        generator.dispose()
        return font
    }

    private fun getHandle(style: FontStyle): FileHandle? {
        return when (style) {
            FontStyle.NORMAL -> normalHandle
            FontStyle.BOLD -> boldHandle
            FontStyle.ITALIC -> italicHandle
        }
    }

    companion object {
        private const val CHARSET = "0123456789qwertzuiopőúasdfghjkléáűíyxcvbnmöüóÖÜÓQWERTZUIOPŐÚASDFGHJKLÉÁŰÍYXCVBNM,.-?:_;>*\\|\$ß@&#><{}'\"+!%/=()©²"
    }
}
