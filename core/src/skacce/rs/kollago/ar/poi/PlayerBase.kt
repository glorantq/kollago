package skacce.rs.kollago.ar.poi

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import ktx.math.vec2
import ktx.math.vec3
import org.oscim.core.GeoPoint
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.protocol.BaseData
import skacce.rs.kollago.utils.ARUtils
import skacce.rs.kollago.utils.newInstance
import skacce.rs.kollago.utils.toGeoPoint
import java.util.*

class PlayerBase(backendData: BaseData, private val vtmMap: VTMMap, private val mapResolutionScale: Int) {
    private companion object {
        private val baseModels: Array<Model?> = arrayOfNulls(40)

        fun getModelForLevel(level: Int): Model {
            if(baseModels[level] != null) {
                return baseModels[level]!!
            }

            val model: Model = ARUtils.loadExternalModel(Gdx.files.internal("base/$level/base.g3db"))
            baseModels[level] = model

            return model
        }
    }

    private var modelInstance: ModelInstance = getModelForLevel(backendData.level).newInstance()
    val rotation = (Random().nextInt(360 * 2) - 180).toFloat()

    var geoPoint: GeoPoint = backendData.coordinates!!.toGeoPoint()
        private set

    var backendData: BaseData
        private set

    val position: Vector2 = vec2()

    init {
        this.backendData = backendData

        val gpsPosition: Vector2 = vtmMap.toWorldPos(geoPoint)

        modelInstance.transform.setToRotation(Vector3.Y, rotation)
        modelInstance.transform.set(Vector3(gpsPosition.x / mapResolutionScale, 0f, gpsPosition.y / mapResolutionScale), modelInstance.transform.getRotation(Quaternion()))
    }

    fun render(modelBatch: ModelBatch) {
        position.set(vtmMap.toWorldPos(geoPoint).scl(1f / mapResolutionScale))

        if (position.dst(Vector2.Zero) >= 512f) {
            return
        }

        modelInstance.transform.setToRotation(Vector3.Y, rotation)
        modelInstance.transform.set(Vector3(position.x, -10f, position.y),  modelInstance.transform.getRotation(Quaternion()))

        val distance = position.dst(0f, 0f)
        val opacity: Float = if (distance <= 50) 1f else 1f - distance / (512 - 50)

        modelInstance.materials.forEach {
            it.set(BlendingAttribute(opacity))
        }

        modelBatch.render(modelInstance)
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

    fun calculatePlayerOpacity(): Float {
        val boundingBox = modelInstance.calculateBoundingBox(BoundingBox())
        boundingBox.mul(modelInstance.transform)

        return boundingBox.getCenter(vec3()).dst(vec3()) / Math.max(boundingBox.width, boundingBox.height)
    }

    fun updateBackendData(newData: BaseData) {
        backendData = newData

        geoPoint = backendData.coordinates!!.toGeoPoint()
        modelInstance = getModelForLevel(backendData.level).newInstance()
    }
}