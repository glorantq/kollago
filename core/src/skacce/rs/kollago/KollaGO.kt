package skacce.rs.kollago

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.tomgrill.gdxdialogs.core.GDXDialogs
import de.tomgrill.gdxdialogs.core.GDXDialogsSystem
import ktx.math.vec2
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.TextureLoadingPerformer
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.graphics.text.Font
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.input.GdxInputHandler
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.network.AuthenticationLoadingPerformer
import skacce.rs.kollago.screens.LoadingScreen
import skacce.rs.kollago.screens.LoginScreen
import kotlin.concurrent.thread

class KollaGO private constructor(val platform: Platform, val textInputProvider: TextInputProvider) : Game() {
    companion object {
        const val WIDTH: Float = 720f
        const val HEIGHT: Float = 1280f

        const val MAP_RESOLUTION: Float = 1024f
        const val MAP_SCALE: Float = 1f

        private var hiddenAreaOffset: Int = -1

        var SAFE_AREA_OFFSET: Int
            get() = hiddenAreaOffset
            private set(value) {
                if(hiddenAreaOffset == -1) {
                    hiddenAreaOffset = value
                }
            }

        private var hiddenInstance: KollaGO? = null

        var INSTANCE: KollaGO
            get() = hiddenInstance ?: throw UninitializedPropertyAccessException("\"hiddenInstance\" was queried before being initialized")
            set(value) {
                if(hiddenInstance == null) {
                    hiddenInstance = value
                } else {
                    throw IllegalAccessException("\"hiddenInstance\" is already initialised!")
                }
            }

        fun create(platform: Platform, textInputProvider: TextInputProvider): KollaGO {
            INSTANCE = KollaGO(platform, textInputProvider)

            return INSTANCE
        }
    }

    lateinit var staticViewport: ExtendViewport
        private set

    lateinit var spriteBatch: SpriteBatch
        private set

    lateinit var assetManager: AssetManager
        private set

    lateinit var textureManager: TextureManager
        private set

    lateinit var textRenderer: TextRenderer
        private set

    lateinit var inputHandler: GdxInputHandler
        private set

    lateinit var dialogs: GDXDialogs
        private set

    override fun create() {
        dialogs = GDXDialogsSystem.install()

        Gdx.app.log("KollaGO", "Setting up 2D graphics...")

        staticViewport = ExtendViewport(WIDTH, HEIGHT)
        staticViewport.apply(true)

        spriteBatch = SpriteBatch()

        assetManager = AssetManager()
        textureManager = TextureManager(assetManager)

        textRenderer = TextRenderer(spriteBatch)
        textRenderer.registerFont(Font("Roboto", "fonts/roboto/Roboto-Regular.ttf", "fonts/roboto/Roboto-Italic.ttf", "fonts/roboto/Roboto-Bold.ttf"))

        inputHandler = GdxInputHandler()

        SAFE_AREA_OFFSET = platform.getSafeAreaOffset()

        Gdx.input.inputProcessor = InputMultiplexer(GestureDetector(inputHandler), inputHandler)
        Gdx.input.isCatchBackKey = true

        Gdx.app.log("KollaGO", "2D setup complete!")
        Gdx.app.log("KollaGO", "Setting up 3D graphics...")

        setScreen(LoadingScreen(TextureLoadingPerformer(assetManager)) {
            if(platform.isLoggedIn()) {
                setScreen(LoadingScreen(AuthenticationLoadingPerformer()) {
                    setScreen(ARWorld())
                }) // TODO
            } else {
                setScreen(LoginScreen())
            }
        })
    }

    override fun render() {
        Gdx.gl.glClearColor(0.55f, 0.72f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        staticViewport.apply(true)
        spriteBatch.projectionMatrix = staticViewport.camera.combined
        spriteBatch.begin()

        screen?.render(Gdx.graphics.deltaTime)

        spriteBatch.end()
    }

    override fun resize(width: Int, height: Int) {
        staticViewport.update(width, height)
        SAFE_AREA_OFFSET = (staticViewport.worldHeight / 2 - staticViewport.unproject(vec2(0f, platform.getSafeAreaOffset().toFloat())).y).toInt()

        screen?.resize(width, height)
    }

    override fun dispose() {
        screen?.dispose()
    }
}
