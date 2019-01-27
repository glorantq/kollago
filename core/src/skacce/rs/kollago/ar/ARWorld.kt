package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.BufferUtils
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.math.vec2
import ktx.math.vec3
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.Platform
import skacce.rs.kollago.ar.overlays.PlayerBaseOverlay
import skacce.rs.kollago.ar.overlays.RewardStopOverlay
import skacce.rs.kollago.ar.poi.PlayerBase
import skacce.rs.kollago.ar.poi.RewardStop
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.NetworkManager
import skacce.rs.kollago.network.protocol.*
import skacce.rs.kollago.utils.*
import java.nio.IntBuffer
import java.util.*

class ARWorld : Screen, InputHandler {
    private fun framebufferSize(): Int = (KollaGO.MAP_RESOLUTION * (KollaGO.MAP_SCALE + 2f)).toInt()

    private val temp: Vector2 = vec2()
    private val temp2: Vector2 = vec2()

    private val game: KollaGO = KollaGO.INSTANCE
    private val networkManager: NetworkManager = game.networkManager

    private val random: Random = Random(System.nanoTime())

    private val camera: PerspectiveCamera = PerspectiveCamera(67f, KollaGO.WIDTH, KollaGO.HEIGHT)
    private val worldViewport: ExtendViewport = ExtendViewport(KollaGO.WIDTH, KollaGO.HEIGHT, camera)
    private val modelBatch: ModelBatch = ModelBatch()
    private val hudRenderer: HUDRenderer = HUDRenderer()

    private val floorMesh: Mesh = ARUtils.createPlaneMesh(KollaGO.MAP_RESOLUTION, KollaGO.MAP_RESOLUTION, 0f, 0f, 1f, 1f)
    private val skyModelInstance: ModelInstance = ModelInstance(ARUtils.createSkySphere())
    private val skyRotation: Int = if (random.nextInt(10) < 5) { 1 } else { -1 }
    private val vtmMap: VTMMap = VTMMap((KollaGO.MAP_RESOLUTION * KollaGO.MAP_SCALE).toInt(), (KollaGO.MAP_RESOLUTION * KollaGO.MAP_SCALE).toInt())
    private val mapFrameBuffer: FrameBuffer = FrameBuffer(Pixmap.Format.RGB565, framebufferSize(), framebufferSize(), false, true)

    private val floorShader: ShaderProgram = ShaderProgram(Gdx.files.internal("shaders/floor.vert"), Gdx.files.internal("shaders/floor.frag"))

    private val cameraController: ARCameraController = ARCameraController(camera, 160, 95)

    private val playerModels: Array<PlayerModel> = arrayOf(
            PlayerModel("deku", "ch00_u00_dammy_skeleton|ch00_avg_normal", "ch00_u00_dammy_skeleton|ch00_run0"),
            PlayerModel("uraraka", "ch02_u00_dammy_skeleton|ch02_avg_normal", "ch02_u00_dammy_skeleton|ch02_run0")
    )

    private val selectedModel: PlayerModel = playerModels[networkManager.ownProfile.model.value]

    private var useCompassRotation: Boolean = true
    private var playerRotation: Float = 0f
    private var lastPlayerRotation: Float = 0f

    private var distance: Float = 0f
    private var timeElapsed: Float = 0f
    private var speed: Float = 0f
    private var targetPoint: GeoPoint? = null

    private val loadedStops: MutableMap<String, RewardStop> = hashMapOf()
    private val loadedBases: MutableMap<String, PlayerBase> = hashMapOf()
    private var lastUpdatePoint: GeoPoint

    private val darkenPixel: Texture
    private var overlayScreen: Overlay? = null

    init {
        val initTrace: Platform.NativePerformanceTrace = game.platform.createTrace("world_load")
        initTrace.start()

        game.inputHandler.addInputHandler(this)
        (Gdx.input.inputProcessor as InputMultiplexer).addProcessor(cameraController)
        Gdx.input.isCatchBackKey = true

        val pixmap: Pixmap = Pixmap(1, 1, Pixmap.Format.RGBA4444)
        pixmap.setColor(0f, 0f, 0f, .4f)
        pixmap.fill()

        darkenPixel = Texture(pixmap)

        camera.position.set(60f, 60f, 60f)
        camera.lookAt(0f, 0f, 0f)
        camera.near = 1f
        camera.far = KollaGO.MAP_RESOLUTION * KollaGO.MAP_SCALE
        camera.update()

        skyModelInstance.materials.get(0).set(IntAttribute(IntAttribute.CullFace, 0))

        vtmMap.setTheme(Gdx.files.internal("map/game_theme.xml"))
        vtmMap.setScale(16)
        vtmMap.setPosition(game.platform.getGpsPosition())

        Gdx.app.log("ARWorld", "3D setup complete!")

        lastUpdatePoint = vtmMap.getLocation()
        actualiseFeatures()

        initTrace.stop()
    }

    override fun render(delta: Float) {
        val renderTrace: Platform.NativePerformanceTrace = game.platform.createTrace("render_world")
        renderTrace.start()

        game.spriteBatch.end()

        temp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        temp.set(game.staticViewport.unproject(temp))
        cameraController.active = overlayScreen == null && !hudRenderer.containsCoordinates(temp.x, temp.y)

        camera.update()
        worldViewport.apply()

        if (useCompassRotation) {
            val newRotation = 180f - Gdx.input.azimuth

            playerRotation = if (Math.abs(lastPlayerRotation - newRotation) > 180) {
                newRotation
            } else {
                lastPlayerRotation + 0.1f * (newRotation - lastPlayerRotation)
            }

            lastPlayerRotation = playerRotation
        }

        val vtmTrace: Platform.NativePerformanceTrace = game.platform.createTrace("render_vtm_map")
        vtmTrace.start()
        mapFrameBuffer.begin()
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.89f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glDisable(GL20.GL_BLEND)

        vtmMap.render()

        Gdx.gl.glEnable(GL20.GL_BLEND)
        mapFrameBuffer.end(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        vtmTrace.stop()

        modelBatch.begin(camera)
        modelBatch.render(skyModelInstance)

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        mapFrameBuffer.colorBufferTexture.bind()
        floorShader.begin()
        floorShader.setUniformMatrix("u_projTrans", camera.combined)
        floorShader.setUniformi("u_texture", 0)
        floorMesh.render(floorShader, GL20.GL_TRIANGLES)
        floorShader.end()

        modelBatch.flush()

        synchronized(loadedStops) {
            loadedStops.forEach {
                it.value.render(modelBatch)
            }
        }

        var lowestPlayerOpacity: Float = 1f
        synchronized(loadedBases) {
            loadedBases.forEach {
                it.value.render(modelBatch)

                val playerOpacity: Float = it.value.calculatePlayerOpacity()
                if(playerOpacity < lowestPlayerOpacity) {
                    lowestPlayerOpacity = playerOpacity
                }
            }
        }

        if(lowestPlayerOpacity <= 0.1f) {
            lowestPlayerOpacity = 0.1f
        }

        modelBatch.flush()

        selectedModel.modelInstance.materials.forEach {
            it.set(BlendingAttribute(lowestPlayerOpacity))
        }

        selectedModel.modelInstance.transform.setFromEulerAngles(playerRotation, 0f, 0f)
        selectedModel.render(modelBatch)

        modelBatch.end()

        skyModelInstance.transform.rotate(Vector3.Y, 2f * skyRotation * delta)

        val mapLocation: GeoPoint = vtmMap.getLocation()

        if (KollaGO.INSTANCE.platform.getGpsPosition() != targetPoint) {
            targetPoint = game.platform.getGpsPosition()

            distance += targetPoint!!.sphericalDistance(mapLocation).toFloat()

            vtmMap.animateToPoint(targetPoint!!)

            useCompassRotation = false
            if(!selectedModel.isRunning()) {
                selectedModel.setRunningAnimation()
            }

            vtmMap.toWorldPos(mapLocation, temp)
            vtmMap.toWorldPos(targetPoint!!, temp2)

            playerRotation = temp2.sub(temp).angle(Vector2.Y)

            System.gc()
        }

        if (mapLocation == targetPoint && selectedModel.isRunning()) {
            useCompassRotation = true
            selectedModel.setIdleAnimation()
        }

        timeElapsed += Gdx.graphics.deltaTime
        if (timeElapsed >= 1) {
            speed = distance

            distance = 0f
            timeElapsed = 0f
        }

        game.staticViewport.apply(true)
        game.spriteBatch.projectionMatrix = KollaGO.INSTANCE.staticViewport.camera.combined
        game.spriteBatch.begin()

        hudRenderer.render()

        if(overlayScreen != null) {
            game.spriteBatch.draw(darkenPixel, 0f, 0f, game.staticViewport.worldWidth, game.staticViewport.worldHeight)
            overlayScreen!!.render()
        }

        game.textRenderer.drawWrappedText("", 10f, worldViewport.worldHeight - 100, 24, "Roboto", FontStyle.NORMAL, Color.RED, worldViewport.worldWidth - 20, Align.topLeft)

        if(targetPoint != null && lastUpdatePoint.sphericalDistance(targetPoint) >= 150) {
            actualiseFeatures()
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            closeOverlay()
        }

        renderTrace.stop()
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        if(overlayScreen != null) {
            if(!overlayScreen!!.boundingBox.contains(x, y)) {
                closeOverlay()
            }

            return true
        }

        val clickTrace: Platform.NativePerformanceTrace = game.platform.createTrace("world_raycast")
        clickTrace.start()

        val pickRay: Ray = camera.getPickRay(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

        synchronized(loadedBases) {
            loadedBases.forEach {
                if(it.value.rayTest(pickRay, camera)) {
                    showOverlay(PlayerBaseOverlay(it.value.backendData))

                    return true
                }
            }
        }

        synchronized(loadedStops) {
            loadedStops.forEach {
                if(it.value.rayTest(pickRay, camera)) {
                    Gdx.app.postRunnable {
                        showOverlay(RewardStopOverlay(it.value.backendData, vtmMap))
                    }

                    return true
                }
            }
        }

        clickTrace.stop()

        return true
    }

    fun showOverlay(overlay: Overlay) {
        if(overlayScreen != null) {
            overlayScreen!!.hide()
        }

        overlayScreen = overlay

        overlayScreen!!.show()
    }

    fun closeOverlay() {
        if(overlayScreen != null) {
            overlayScreen!!.hide()

            overlayScreen = null
        }
    }

    fun createOrUpdateStop(stop: StopData) {
        synchronized(loadedStops) {
            networkManager.actualiseTimeout(stop)
            loadedStops[stop.stopId] = RewardStop(stop.coordinates!!.toGeoPoint(), vtmMap, KollaGO.MAP_SCALE, stop)
        }
    }

    fun createOrUpdateBase(base: BaseData) {
        synchronized(loadedBases) {
            if(loadedBases.containsKey(base.baseId)) {
                loadedBases[base.baseId]!!.updateBackendData(base)
            } else {
                loadedBases[base.baseId] = PlayerBase(base, vtmMap, KollaGO.MAP_SCALE.toInt())
            }
        }
    }

    private fun removeFarFeatures() {
        synchronized(loadedStops) {
            val keysToRemove: MutableList<String> = arrayListOf()
            loadedStops.forEach {
                if(it.value.geoPoint.sphericalDistance((if(targetPoint != null) targetPoint else vtmMap.getLocation())!!) > 1000) {
                    keysToRemove.add(it.key)
                }
            }

            keysToRemove.forEach {
                loadedStops.remove(it)
            }

            Gdx.app.log("ARWorld", "Removed ${keysToRemove.size} stops!")
        }

        synchronized(loadedBases) {
            val keysToRemove: MutableList<String> = arrayListOf()
            loadedBases.forEach {
                if(it.value.geoPoint.sphericalDistance((if(targetPoint != null) targetPoint else vtmMap.getLocation())!!) > 1000) {
                    keysToRemove.add(it.key)
                }
            }

            keysToRemove.forEach {
                loadedBases.remove(it)
            }

            Gdx.app.log("ARWorld", "Removed ${keysToRemove.size} bases!")
        }
    }

    private fun actualiseFeatures() {
        val featuresTrace: Platform.NativePerformanceTrace = game.platform.createTrace("world_actualise_features")
        featuresTrace.start()

        val location: Coordinates = (if(targetPoint != null) targetPoint else vtmMap.getLocation())!!.toCoordinates()

        networkManager.actualiseFeatures(location)
        removeFarFeatures()

        lastUpdatePoint = location.toGeoPoint()
        System.gc()

        featuresTrace.stop()
    }

    override fun resize(width: Int, height: Int) {
        worldViewport.update(width, height)
    }

    override fun dispose() {
        modelBatch.dispose()
        floorMesh.dispose()
        mapFrameBuffer.dispose()
        floorShader.dispose()
        hudRenderer.dispose()

        closeOverlay()

        game.inputHandler.removeInputHandler(this)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun show() {}

    interface Overlay {
        val boundingBox: Rectangle

        fun show()
        fun hide()
        fun render()
    }
}