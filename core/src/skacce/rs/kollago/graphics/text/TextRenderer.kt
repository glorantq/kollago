package skacce.rs.kollago.graphics.text

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import ktx.math.vec2
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit


class TextRenderer(private val spriteBatch: SpriteBatch) {
    private val loadedFonts: MutableList<Font> = CopyOnWriteArrayList<Font>()

    private val fontCache: LoadingCache<CacheLookup, BitmapFont> = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build(object : CacheLoader<CacheLookup, BitmapFont>() {
                override fun load(key: CacheLookup): BitmapFont {
                    return loadedFonts.find { it.name == key.name }?.getFont(key.style, key.size) ?: throw RuntimeException("Invalid font: ${key.name}")
                }
            })

    private val glyphLayout: GlyphLayout = GlyphLayout()

    fun registerFont(font: Font) {
        loadedFonts.add(font)

        Gdx.app.log("TextRenderer", "Registered font: $font")
    }

    fun drawText(text: String, x: Float, y: Float, size: Int, fontName: String, fontStyle: FontStyle, color: Color, yBottom: Boolean) {
        var y0: Float = y

        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return

        if (yBottom) {
            y0 += size.toFloat()
        }

        font.color = color
        font.draw(spriteBatch, text, x, y0)
    }

    fun drawCenteredText(text: String, x: Float, y: Float, size: Int, fontName: String, fontStyle: FontStyle, color: Color) {
        var x0: Float = x
        var y0: Float = y

        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return

        glyphLayout.setText(font, text)

        x0 -= glyphLayout.width / 2
        y0 += glyphLayout.height / 2

        font.color = color
        font.draw(spriteBatch, text, x0, y0)
    }

    fun drawRightText(text: String, x: Float, y: Float, size: Int, fontName: String, fontStyle: FontStyle, color: Color, yBottom: Boolean) {
        var x0: Float = x
        var y0: Float = y

        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return

        glyphLayout.setText(font, text)

        x0 -= glyphLayout.width

        if (yBottom) {
            y0 += size.toFloat()
        }

        font.color = color
        font.draw(spriteBatch, text, x0, y0)
    }

    fun drawWrappedText(text: String, x: Float, y: Float, size: Int, fontName: String, fontStyle: FontStyle, color: Color, width: Float, align: Int) {
        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return

        font.color = color
        font.draw(spriteBatch, text, x, y, width, align, true)
    }

    fun getTextSize(text: String, fontName: String, fontStyle: FontStyle, size: Int): Vector2 {
        if (text.isEmpty()) {
            return Vector2.Zero
        }

        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return Vector2.Zero

        glyphLayout.setText(font, text)

        return vec2(x = glyphLayout.width, y = glyphLayout.height)
    }


    fun getWrappedTextSize(text: String, fontName: String, fontStyle: FontStyle, size: Int, width: Float, align: Int): Vector2 {
        if (text.isEmpty()) {
            return Vector2.Zero
        }

        val font = fontCache.get(CacheLookup(fontName, fontStyle, size)) ?: return Vector2.Zero

        glyphLayout.setText(font, text, Color.WHITE, width, align, true)

        return vec2(x = glyphLayout.width, y = glyphLayout.height)
    }

    private data class CacheLookup(val name: String, val style: FontStyle, val size: Int)
}
