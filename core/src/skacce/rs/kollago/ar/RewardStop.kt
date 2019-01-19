package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import org.oscim.core.GeoPoint
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.protocol.StopData
import java.util.*

class RewardStop(stopModel: Model, val geoPoint: GeoPoint, private val vtmMap: VTMMap, private val mapResolutionScale: Float, val backendData: StopData) {
    private val modelInstance: ModelInstance
    private val controller: AnimationController

    private val rotationDirection = (if (Random().nextInt(10) < 5) -1 else 1).toFloat()
    private var rotation = (Random().nextInt(360 * 2) - 180).toFloat()

    val position = Vector2()

    init {
        val gpsPosition: Vector2 = vtmMap.toWorldPos(geoPoint)

        modelInstance = ModelInstance(stopModel)
        modelInstance.transform.set(Vector3(gpsPosition.x / mapResolutionScale, 15f, gpsPosition.y / mapResolutionScale), Quaternion())
        modelInstance.transform.scale(3f, 3f, 3f)

        controller = AnimationController(modelInstance)
        controller.setAnimation("ch_random_body_skeleton|ch_random", -1)
    }

    fun render(modelBatch: ModelBatch, environment: Environment) {
        position.set(vtmMap.toWorldPos(geoPoint).scl(1f / mapResolutionScale))

        if (position.dst(Vector2.Zero) >= 512f) {
            return
        }

        controller.update(Gdx.graphics.deltaTime)

        rotation += 15f * Gdx.graphics.deltaTime * rotationDirection
        if (rotation >= 360) {
            rotation = 0f
        }

        modelInstance.transform.setToRotation(Vector3.Y, rotation)
        modelInstance.transform.set(Vector3(position.x, 10f, position.y), modelInstance.transform.getRotation(Quaternion()))

        val distance = position.dst(0f, 0f)
        val opacity: Float = if (distance <= 50) 1f else 1f - distance / (512 - 50)

        modelInstance.materials.get(0).set(BlendingAttribute(opacity))

        modelBatch.render(modelInstance, environment)
    }

    fun rayTest(ray: Ray): Boolean {
        val pos = vtmMap.toWorldPos(geoPoint).scl(1f / mapResolutionScale)

        if (pos.dst(Vector2.Zero) >= 512f) {
            return false
        }

        val boundingBox = modelInstance.calculateBoundingBox(BoundingBox())
        boundingBox.mul(modelInstance.transform)

        return Intersector.intersectRayBoundsFast(ray, boundingBox)
    }
}