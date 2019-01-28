package skacce.rs.kollago.ar.overlays

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.collections.gdxArrayOf
import ktx.math.vec2
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.RepeatedNinePatch
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.network.protocol.StopData
import skacce.rs.kollago.utils.toCoordinates
import skacce.rs.kollago.utils.toGeoPoint
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class RewardStopOverlay(private val stop: StopData, private val vtmMap: VTMMap) : ARWorld.Overlay, InputHandler {
    private companion object {
        private val greyscaleShader: ShaderProgram = ShaderProgram(Gdx.files.internal("shaders/greyscale.vert"), Gdx.files.internal("shaders/greyscale.frag"))

        private val animationTexture: Texture = KollaGO.INSTANCE.textureManager["gui/spinning_coin.png"]
        private val spinningCoin: Animation<TextureRegion>

        init {
            val frames: MutableList<TextureRegion> = arrayListOf()

            TextureRegion.split(animationTexture, 390, 390).forEach {
                it.forEach {
                    frames.add(it)
                }
            }

            spinningCoin = Animation(1f / 60f, gdxArrayOf(*frames.subList(0, frames.size - 6).toTypedArray()), Animation.PlayMode.LOOP)
        }
    }

    override val boundingBox: Rectangle = Rectangle()

    private val game: KollaGO = KollaGO.INSTANCE

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()

    private val random: Random = Random()

    private val viewport: Viewport = game.staticViewport

    private lateinit var nameBackground: RepeatedNinePatch
    private val background: NinePatch = NinePatch(game.textureManager["gui/menu_bg.png"], 20, 20, 20, 20)
    private val redColour: Color = Color(196f / 255f, 15f / 255f, 15f / 255f, 1f)

    private var coinStateTime: Float = 0f
    private val geoPoint: GeoPoint = stop.coordinates!!.toGeoPoint()

    private val coinBounds: Rectangle = Rectangle()
    private val itemBubbles: MutableList<ItemBubble> = CopyOnWriteArrayList()

    override fun show() {
        val width: Float = viewport.worldWidth - 90f
        val height: Float = (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f + 20f

        temp.set(game.textRenderer.getWrappedTextSize(stop.name, "Hemi", FontStyle.NORMAL, 35, width - 60f, Align.center))

        nameBackground = RepeatedNinePatch("gui/button_normal.png", "gui/button_normal_repeat.png", width.toInt() - 20, 47, 47, 50, 50)
        boundingBox.set(viewport.worldWidth / 2 - width / 2, (viewport.worldHeight - KollaGO.SAFE_AREA_OFFSET) / 2f - height / 2, width, height)

        game.inputHandler.addInputHandler(this)
    }

    override fun hide() {
        game.inputHandler.removeInputHandler(this)

        synchronized(itemBubbles) {
            itemBubbles.clear()
        }
    }

    override fun render() {
        coinStateTime += Gdx.graphics.deltaTime

        background.draw(game.spriteBatch, viewport.worldWidth / 2 - boundingBox.width / 2, viewport.worldHeight / 2 - boundingBox.height / 2, boundingBox.width, boundingBox.height)

        temp.set(game.textRenderer.getWrappedTextSize(stop.name, "Hemi", FontStyle.NORMAL, 35, boundingBox.width - 60f, Align.center))

        val nameHeight: Float = temp.y

        temp2.set(boundingBox.width - 40f, temp.y + 50f)
        temp.set(boundingBox.x + boundingBox.width / 2 - temp2.x / 2f, boundingBox.y + boundingBox.height - 20f - (temp.y + 50f) / 2 - temp.y / 2)

        nameBackground.draw(game.spriteBatch, temp.x, temp.y, temp2.x, temp2.y)
        game.textRenderer.drawWrappedText(stop.name, temp.x + 10f, temp.y + temp2.y / 2f + nameHeight / 2, 35, "Hemi", FontStyle.NORMAL, Color.WHITE, temp2.x - 20f, Align.center)

        val distance: Float = geoPoint.sphericalDistance(vtmMap.getLocation()).toFloat()
        val stopTimeout: Long = game.networkManager.getStopTimeout(stop)

        if(stopTimeout - System.currentTimeMillis() > 0 || distance > 50f) {
            game.spriteBatch.shader = greyscaleShader
        }

        coinBounds.set(boundingBox.x + boundingBox.width / 2 - 390f / 2f, temp.y - 390f - 60f, 390f, 390f)
        game.spriteBatch.draw(spinningCoin.getKeyFrame(coinStateTime), coinBounds.x, coinBounds.y, coinBounds.width, coinBounds.height)

        game.spriteBatch.shader = null

        val statusMessage: String = when {
            distance > 50f -> "Túl messze vagy!"

            stopTimeout - System.currentTimeMillis() > 0 -> {
                var seconds: Int = (stopTimeout - System.currentTimeMillis()).toInt() / 1000
                val minutes: Int = seconds / 60

                seconds -= minutes * 60

                String.format(Locale.getDefault(), "A stop %02d:%02d múlva lesz használható", minutes, seconds)
            }

            else -> ""
        }

        if(statusMessage.isNotBlank()) {
            temp2.set(temp2.x, 75f)
            temp.set(temp.x, temp.y - 390f - 120f - temp2.y)
            nameBackground.draw(game.spriteBatch, temp.x, temp.y, temp2.x, temp2.y)

            game.textRenderer.drawCenteredText(statusMessage, temp.x + temp2.x / 2, temp.y + temp2.y / 2, 30, "Hemi", FontStyle.NORMAL, redColour)
        }

        synchronized(itemBubbles) {
            itemBubbles.forEach {
                it.render()
            }
        }
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if(coinBounds.contains(x, y)) {
            game.networkManager.collectStop(stop, vtmMap.getLocation()) {
                val currentProfile: ProfileData = game.networkManager.ownProfile

                var coinsGained: Long = it.coins - currentProfile.coins

                game.networkManager.applyProfileUpdate(it)

                val coin100s: Int = (coinsGained / 100).toInt()
                coinsGained %= 100

                val coin10s: Int = (coinsGained / 10).toInt()
                coinsGained %= 10

                val coin1s: Int = coinsGained.toInt()

                for(i: Int in 0 until coin100s) {
                    createItemBubble("gui/coin_100.png")
                }

                for(i: Int in 0 until coin10s) {
                    createItemBubble("gui/coin_10.png")
                }

                for(i: Int in 0 until coin1s) {
                    createItemBubble("gui/coin_1.png")
                }

                game.networkManager.updateBase(game.networkManager.ownProfile.baseId)
            }
        }

        synchronized(itemBubbles) {
            for(bubble: ItemBubble in itemBubbles) {
                if(bubble.click(x, y)) {
                    break
                }
            }
        }

        return true
    }

    private fun createItemBubble(texture: String) {
        Gdx.app.postRunnable {
            synchronized(itemBubbles) {
                val baseX: Float = coinBounds.x
                val baseY: Float = coinBounds.y

                temp.set(baseX + Math.max((random.nextInt(coinBounds.width.toInt() * 2) - coinBounds.width).toDouble(), 0.0).toFloat(),
                        baseY + Math.max((random.nextInt(coinBounds.height.toInt() * 2) - coinBounds.height).toDouble(), 0.0).toFloat())

                val bubble: ItemBubble = ItemBubble(texture, temp.cpy())
                bubble.onClickHandler = {
                    synchronized(itemBubbles) {
                        itemBubbles.remove(bubble)
                    }
                }

                itemBubbles.add(bubble)
            }
        }
    }

    private class ItemBubble(itemTexture: String, private val initialPosition: Vector2) {
        private companion object {
            private val game: KollaGO = KollaGO.INSTANCE

            private val bubbleTexture: Texture = game.textureManager["gui/item_bubble.png"]
            private val bubbleSize: Vector2 = vec2(128f, 128f)
            private val textureSize: Vector2 = bubbleSize.cpy().sub(40f, 40f)

            private val random: Random = Random(System.nanoTime())
        }

        private val texture: Texture = game.textureManager[itemTexture]

        lateinit var onClickHandler: () -> Unit

        private val position: Vector2 = Vector2(initialPosition)

        private val timeOffset: Int = random.nextInt(100)
        private val directionX: Int = if(random.nextBoolean()) 1 else -1
        private val directionY: Int = if(random.nextBoolean()) 1 else -1

        fun render() {
            val time: Double = (Gdx.graphics.frameId + timeOffset).toDouble() / 100.0

            val newX: Float = initialPosition.x + (directionX * Math.sin(time) * bubbleSize.x / 4f).toFloat()
            val newY: Float = initialPosition.y + (directionY * Math.cos(time) * bubbleSize.y / 8f).toFloat()

            position.set(newX, newY)

            game.spriteBatch.draw(texture, position.x + 20f, position.y + 20f, textureSize.x, textureSize.y)
            game.spriteBatch.draw(bubbleTexture, position.x, position.y, bubbleSize.x, bubbleSize.y)
        }

        fun click(x: Float, y: Float): Boolean {
            if(Rectangle.tmp.set(position.x, position.y, bubbleSize.x, bubbleSize.y).contains(x, y)) {
                if(::onClickHandler.isInitialized) {
                    onClickHandler()
                }

                return true
            }

            return false
        }
    }
}