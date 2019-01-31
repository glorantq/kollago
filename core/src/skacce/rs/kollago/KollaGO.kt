package skacce.rs.kollago

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.utils.viewport.ExtendViewport
import de.tomgrill.gdxdialogs.core.GDXDialogs
import de.tomgrill.gdxdialogs.core.GDXDialogsSystem
import ktx.math.vec2
import skacce.rs.kollago.graphics.TextureLoadingPerformer
import skacce.rs.kollago.graphics.TextureManager
import skacce.rs.kollago.graphics.text.Font
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.input.GdxInputHandler
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.network.NetworkManager
import skacce.rs.kollago.screens.LoadingScreen
import skacce.rs.kollago.screens.LoginScreen

/**
 * KollaGO fő file
 *
 * Elindítja a játékot, betölti a textúrákat, és bejelentkezteti a játékost.
 * A bejelentkezés és GPS inicializáció után a játék fő logikája átkerül az @see skacce.rs.kollago.ar.ARWorld fileba
 *
 * Bejelentkezés során a játék először a Google szerverein hitelesíti magát a Firebase szolgáltatással, majd az itt
 * kapott azonosítóval hitelesíti magát a játék fő szerverén. Ezzel megkapja annak a szervernek a címét, amin maga
 * a játék történik. Ez a többszerveres megoldás optimalizáció miatt szükséges. Miután csatlakozott a játékszerverre,
 * lekéri a közelben lévő bázisokat és állomásokat, valamint a játékos profiladatait (mindet a Firebase azonosítóval).
 *
 * Játék során minden üzetet tartalmazza a titkosított Firebase azonosítót, amit a szerverek tudnak dekódolni, ezzel
 * azonosítva a játékosokat.
 *
 * A bázisokat a fő szerver kezeli, így akárhol is jelentkezik be valaki, azokat mindenképpen látni fogja. Az állomásokat
 * a játékszerverek kezelik, így nincs nagy teher egy adatbázison sem. A támadásokat a közponi szerver kezeli.
 *
 * A hálózati protokoll alapja a Kryonet és Protobuf. Protobuf fájklokból állnak össze az üzenetek (common.proto,
 * gameplay.proto, authentication.proto), a Kryonet pedig a TCP kapcsolatot biztosítja.
 *
 * A saját zászlókat is a központi szerver látja el, egy HTTP endpoint segítségével. Ehhez szintén
 * szükséges a Firebase azonosító. A játék onnan letölti a megfelelő textúrákat, majd beállítja
 * őket a megfelelő zászlóra.
 *
 * TL;DR: A játékosok pénzt gyűjtenek állomásokon, amikből megtámadhatják egymást
 *
 * @author Gerber Lóránt Viktor
 * @since 1.0
 */

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

    lateinit var networkManager: NetworkManager
        private set

    override fun create() {
        val initTrace: Platform.NativePerformanceTrace = platform.createTrace("game_init")
        initTrace.start()

        dialogs = GDXDialogsSystem.install()

        Gdx.app.log("KollaGO", "Setting up 2D graphics...")

        staticViewport = ExtendViewport(WIDTH, HEIGHT)
        staticViewport.apply(true)

        spriteBatch = SpriteBatch()

        assetManager = AssetManager()
        textureManager = TextureManager(assetManager)

        textRenderer = TextRenderer(spriteBatch)
        textRenderer.registerFont(Font("Roboto", "fonts/roboto/Roboto-Regular.ttf", "fonts/roboto/Roboto-Italic.ttf", "fonts/roboto/Roboto-Bold.ttf"))
        textRenderer.registerFont(Font("Hemi", "fonts/hemi/hemi head bd it.ttf", null, null))

        inputHandler = GdxInputHandler()

        SAFE_AREA_OFFSET = platform.getSafeAreaOffset()

        Gdx.input.inputProcessor = InputMultiplexer(GestureDetector(inputHandler), inputHandler)
        Gdx.input.isCatchBackKey = true

        Gdx.app.log("KollaGO", "2D setup complete!")

        networkManager = NetworkManager()

        setScreen(LoadingScreen(TextureLoadingPerformer(assetManager)) {
            if(platform.isLoggedIn()) {
                networkManager.beginLoginSequence()
            } else {
                setScreen(LoginScreen())
            }
        })

        initTrace.stop()
    }

    override fun render() {
        Gdx.gl.glClearColor(0.55f, 0.72f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or GL20.GL_STENCIL_BUFFER_BIT)

        Gdx.gl.glStencilMask(0xFF)

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
