package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.NetworkManager
import skacce.rs.kollago.network.protocol.Coordinates
import skacce.rs.kollago.network.protocol.NearStops
import skacce.rs.kollago.network.protocol.StopData
import skacce.rs.kollago.utils.ARUtils
import skacce.rs.kollago.utils.newInstance
import skacce.rs.kollago.utils.toCoordinates
import skacce.rs.kollago.utils.toGeoPoint
import java.util.*

class ARWorld : Screen {
    private fun framebufferSize(): Int = (KollaGO.MAP_RESOLUTION * (KollaGO.MAP_SCALE + 2f)).toInt()

    private val game: KollaGO = KollaGO.INSTANCE
    private val networkManager: NetworkManager = game.networkManager

    private val random: Random = Random(System.nanoTime())

    private val camera: PerspectiveCamera = PerspectiveCamera(67f, KollaGO.WIDTH, KollaGO.HEIGHT)
    private val worldViewport: ExtendViewport = ExtendViewport(KollaGO.WIDTH, KollaGO.HEIGHT, camera)
    private val modelBatch: ModelBatch = ModelBatch()
    private val environment: Environment = Environment()

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
    private val animationController: AnimationController = AnimationController(selectedModel.modelInstance)

    private val stopModel: Model = ARUtils.loadExternalModel(Gdx.files.internal("random/random.g3db"))

    private var useCompassRotation: Boolean = true
    private var playerRotation: Float = 0f
    private var lastPlayerRotation: Float = 0f

    private var distance: Float = 0f
    private var timeElapsed: Float = 0f
    private var speed: Float = 0f
    private var targetPoint: GeoPoint? = null

    private val loadedStops: MutableMap<String, RewardStop> = hashMapOf()
    private var lastUpdatePoint: GeoPoint

    private var selectedStop: StopData = StopData()

    init {
        (Gdx.input.inputProcessor as InputMultiplexer).addProcessor(cameraController)

        camera.position.set(60f, 60f, 60f)
        camera.lookAt(0f, 0f, 0f)
        camera.near = 1f
        camera.far = KollaGO.MAP_RESOLUTION * KollaGO.MAP_SCALE
        camera.update()

        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f))
        environment.add(DirectionalLight().set(0.2f, 0.2f, 0.2f, -1f, -0.8f, -0.2f))

        skyModelInstance.materials.get(0).set(IntAttribute(IntAttribute.CullFace, 0))

        vtmMap.setTheme(Gdx.files.internal("map/game_theme.xml"))
        vtmMap.setScale(16)
        vtmMap.setPosition(game.platform.getGpsPosition())

        animationController.setAnimation(selectedModel.runningAnimation, -1)

        Gdx.app.log("ARWorld", "3D setup complete!")

        lastUpdatePoint = vtmMap.getLocation()
        actualiseFeatures()
    }

    override fun render(delta: Float) {
        game.spriteBatch.end()

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

        selectedModel.modelInstance.transform.setFromEulerAngles(playerRotation, 0f, 0f)

        mapFrameBuffer.begin()
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.89f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glDisable(GL20.GL_BLEND)

        vtmMap.render()

        Gdx.gl.glEnable(GL20.GL_BLEND)
        mapFrameBuffer.end(0, 0, Gdx.graphics.width, Gdx.graphics.height)

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
                it.value.render(modelBatch, environment)
            }
        }

        modelBatch.render(selectedModel.modelInstance, environment)

        modelBatch.end()

        skyModelInstance.transform.rotate(Vector3.Y, 2f * skyRotation * delta)

        animationController.update(delta)

        if (KollaGO.INSTANCE.platform.getGpsPosition() != targetPoint) {
            targetPoint = game.platform.getGpsPosition()

            distance += targetPoint!!.sphericalDistance(vtmMap.getLocation()).toFloat()

            vtmMap.animateToPoint(targetPoint!!)

            useCompassRotation = false
            if (!animationController.current.animation.id.equals(selectedModel.runningAnimation, ignoreCase = true)) {
                animationController.setAnimation(selectedModel.runningAnimation, -1)
            }

            val current = vtmMap.getLocation()
            val mapPositionCurrent = vtmMap.toWorldPos(current)
            val mapPositionTarget = vtmMap.toWorldPos(targetPoint!!)

            playerRotation = mapPositionTarget.sub(mapPositionCurrent).angle(Vector2.Y)
        }

        if (vtmMap.getLocation() == targetPoint && animationController.current.animation.id.equals(selectedModel.runningAnimation, ignoreCase = true)) {
            useCompassRotation = true
            animationController.setAnimation(selectedModel.idleAnimation, -1)
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

        game.textRenderer.drawWrappedText(selectedStop.toString(), 10f, worldViewport.worldHeight - 100, 24, "Roboto", FontStyle.NORMAL, Color.RED, worldViewport.worldWidth - 20, Align.topLeft)

        if(Gdx.input.justTouched()) {
            synchronized(loadedStops) {
                val pickRay: Ray = camera.getPickRay(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

                loadedStops.forEach {
                    if(it.value.rayTest(pickRay)) {
                        selectedStop = it.value.backendData
                        return@forEach
                    }
                }
            }
        }

        if(targetPoint != null && lastUpdatePoint.sphericalDistance(targetPoint) >= 150) {
            actualiseFeatures()
        }
    }

    fun createStop(stop: StopData) {
        synchronized(loadedStops) {
            loadedStops[stop.stopId] = RewardStop(stopModel, stop.coordinates!!.toGeoPoint(), vtmMap, KollaGO.MAP_SCALE, stop)
        }
    }

    private fun removeFarStops() {
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
    }

    private fun actualiseFeatures() {
        val location: Coordinates = (if(targetPoint != null) targetPoint else vtmMap.getLocation())!!.toCoordinates()
        networkManager.packetHandler.sendPacket(NearStops(networkManager.firebaseUid, location), "", networkManager.kryoClient)
        removeFarStops()

        lastUpdatePoint = location.toGeoPoint()
        System.gc()
    }

    override fun resize(width: Int, height: Int) {
        worldViewport.update(width, height)
    }

    override fun dispose() {
        modelBatch.dispose()
        floorMesh.dispose()
        mapFrameBuffer.dispose()
        floorShader.dispose()
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun show() {}
}