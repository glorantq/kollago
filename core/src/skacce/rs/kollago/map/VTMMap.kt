package skacce.rs.kollago.map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ktx.math.vec2
import okhttp3.OkHttpClient
import org.oscim.backend.GL
import org.oscim.backend.GLAdapter.gl
import org.oscim.core.GeoPoint
import org.oscim.core.MapPosition
import org.oscim.core.Point
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.map.Map
import org.oscim.renderer.GLState
import org.oscim.renderer.MapRenderer
import org.oscim.theme.IRenderTheme
import org.oscim.theme.ThemeFile
import org.oscim.theme.XmlRenderThemeMenuCallback
import org.oscim.tiling.TileSource
import org.oscim.tiling.source.OkHttpEngine
import org.oscim.tiling.source.mvt.OpenMapTilesMvtTileSource
import org.oscim.utils.animation.Easing
import java.io.InputStream

class VTMMap(private val width: Int, private val height: Int) {
    private val map: MapAdapter
    private val mapRenderer: MapRenderer

    private var scale: Int = 1

    private val tileSource: TileSource

    init {
        map = MapAdapter()
        mapRenderer = MapRenderer(map)

        map.viewport().setViewSize(width, height)
        mapRenderer.onSurfaceCreated()
        mapRenderer.onSurfaceChanged(width, height)

        val builder: OkHttpClient.Builder = OkHttpClient.Builder()

        val factory: OkHttpEngine.OkHttpFactory = OkHttpEngine.OkHttpFactory(builder)

        this.tileSource = OpenMapTilesMvtTileSource.builder()
                .url("https://mapserver.skacce.rs/data/v3")
                .tilePath("/{Z}/{X}/{Y}.pbf")
                .httpFactory(factory)
                .build()

        val vectorTileLayer: VectorTileLayer = VectorTileLayer(map, 64)

        vectorTileLayer.setTileSource(tileSource)
        map.layers().add(0, vectorTileLayer)

        setTheme(Gdx.files.internal("map/theme.xml"))
    }

    fun render() {
        gl.frontFace(GL.CW)
        mapRenderer.onDrawFrame()
        gl.frontFace(GL.CCW)

        GLState.enableVertexArrays(-1, -1)
        gl.disable(GL.DEPTH_TEST)
        gl.disable(GL.STENCIL_TEST)
        gl.flush()
    }

    fun setTheme(theme: FileHandle) {
        map.setTheme(object : ThemeFile {
            private var menuCallback: XmlRenderThemeMenuCallback? = null

            override fun getMenuCallback(): XmlRenderThemeMenuCallback? {
                return menuCallback
            }

            override fun getRelativePathPrefix(): String {
                return ""
            }

            @Throws(IRenderTheme.ThemeException::class)
            override fun getRenderThemeAsStream(): InputStream {
                return theme.read()
            }

            override fun isMapsforgeTheme(): Boolean {
                return false
            }

            override fun setMenuCallback(menuCallback: XmlRenderThemeMenuCallback) {
                this.menuCallback = menuCallback
            }
        })

        Gdx.app.log("Map", "Set theme!")
    }

    fun setPosition(position: GeoPoint?) {
        if (position == null) {
            Gdx.app.error("Map", "GeoPoint is null!")
            return
        }

        setPosition(position.latitude, position.longitude)
    }

    fun setPosition(latitude: Double, longitude: Double) {
        map.setMapPosition(latitude, longitude, Math.pow(2.0, scale.toDouble()))
        map.viewport().setRotation(0.0)
        map.updateMap(true)
    }

    fun setScale(scale: Int) {
        this.scale = scale
        setPosition(map.mapPosition.geoPoint)
    }

    fun toWorldPos(geoPoint: GeoPoint, out: Vector2 = vec2()): Vector2 {
        val point = Point()
        map.viewport().toScreenPoint(geoPoint, true, point)

        out.set(point.x.toInt().toFloat(), point.y.toInt().toFloat())
        return out
    }

    fun animateToPoint(point: GeoPoint) {
        map.animator().animateTo(1000, MapPosition(point.latitude, point.longitude, Math.pow(2.0, scale.toDouble())), Easing.Type.SINE_OUT)
        map.updateMap(true)

        Gdx.app.log("Map", "Animating to: $point")
    }

    fun getLocation(): GeoPoint {
        return map.mapPosition.geoPoint
    }

    private inner class MapAdapter : Map() {
        private var mRenderRequest: Boolean = false
        private var mRenderWait: Boolean = false

        private val mRedrawCb = Runnable {
            prepareFrame()
            Gdx.graphics.requestRendering()
        }

        override fun getWidth(): Int {
            return this@VTMMap.width
        }

        override fun getHeight(): Int {
            return this@VTMMap.height
        }

        override fun getScreenWidth(): Int {
            return Gdx.graphics.displayMode.width
        }

        override fun getScreenHeight(): Int {
            return Gdx.graphics.displayMode.height
        }

        override fun updateMap(forceRender: Boolean) {
            synchronized(mRedrawCb) {
                if (!mRenderRequest) {
                    mRenderRequest = true
                    Gdx.app.postRunnable(mRedrawCb)
                } else {
                    mRenderWait = true
                }
            }
        }

        override fun render() {
            synchronized(mRedrawCb) {
                mRenderRequest = true
                if (mClearMap)
                    updateMap(false)
                else {
                    Gdx.graphics.requestRendering()
                }
            }
        }

        override fun post(runnable: Runnable): Boolean {
            Gdx.app.postRunnable(runnable)
            return true
        }

        override fun postDelayed(action: Runnable, delay: Long): Boolean {
            Timer.schedule(object : Timer.Task() {
                override fun run() {
                    action.run()
                }
            }, delay / 1000f)
            return true
        }

        override fun beginFrame() {}

        override fun doneFrame(animate: Boolean) {
            synchronized(mRedrawCb) {
                mRenderRequest = false
                if (animate || mRenderWait) {
                    mRenderWait = false
                    updateMap(true)
                }
            }
        }
    }
}