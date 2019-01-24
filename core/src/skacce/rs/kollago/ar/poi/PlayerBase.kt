package skacce.rs.kollago.ar.poi

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
import ktx.math.div
import ktx.math.vec2
import ktx.math.vec3
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.map.VTMMap
import skacce.rs.kollago.network.protocol.BaseData
import skacce.rs.kollago.utils.ARUtils
import skacce.rs.kollago.utils.newInstance
import skacce.rs.kollago.utils.toGeoPoint
import java.util.*

class PlayerBase(backendData: BaseData, private val vtmMap: VTMMap, private val mapResolutionScale: Int) {
    private companion object {
        private val flagModel: Model = ARUtils.loadExternalModel(Gdx.files.internal("base/flag.g3db"))
        private val grassModel: Model = ARUtils.loadExternalModel(Gdx.files.internal("base/grass.g3db"))
    }

    private val temp: Vector3 = vec3()

    private var flagInstance: ModelInstance = flagModel.newInstance()
    private var grassInstance: ModelInstance = grassModel.newInstance()

    val rotation = (Random().nextInt(360 * 2) - 180).toFloat()

    var geoPoint: GeoPoint = backendData.coordinates!!.toGeoPoint()
        private set

    var backendData: BaseData
        private set

    val position: Vector2 = vec2()

    init {
        this.backendData = backendData

        val gpsPosition: Vector2 = vtmMap.toWorldPos(geoPoint) / mapResolutionScale

        transform(gpsPosition, rotation)
    }

    fun render(modelBatch: ModelBatch) {
        position.set(vtmMap.toWorldPos(geoPoint) / mapResolutionScale)

        if (position.dst(Vector2.Zero) >= 512f) {
            return
        }

        transform(position, rotation)

        val distance = position.dst(0f, 0f)
        val opacity: Float = if (distance <= 50) 1f else 1f - distance / (512 - 50)

        flagInstance.materials.forEach {
            it.set(BlendingAttribute(opacity))
        }

        grassInstance.materials.forEach {
            it.set(BlendingAttribute(opacity))
        }

        modelBatch.render(grassInstance)
        modelBatch.render(flagInstance)
    }

    private fun transform(position: Vector2, rotation: Float) {
        flagInstance.transform.setToRotation(Vector3.Y, rotation)
        grassInstance.transform.setToRotation(Vector3.Y, rotation)

        flagInstance.transform.set(Vector3(position.x, 0f, position.y), flagInstance.transform.getRotation(Quaternion()))
        grassInstance.transform.set(Vector3(position.x, 0f, position.y), grassInstance.transform.getRotation(Quaternion()))
    }

    fun rayTest(ray: Ray, camera: PerspectiveCamera): Boolean {
        val boundingBox = flagInstance.calculateBoundingBox(BoundingBox())
        boundingBox.mul(flagInstance.transform)

        if(!camera.frustum.boundsInFrustum(boundingBox)) {
            return false
        }

        val pos = vtmMap.toWorldPos(geoPoint) / mapResolutionScale

        if (pos.dst(Vector2.Zero) >= 512f) {
            return false
        }



        return Intersector.intersectRayBoundsFast(ray, boundingBox)
    }

    fun calculatePlayerOpacity(): Float {
        val boundingBox = flagInstance.calculateBoundingBox(BoundingBox())
        boundingBox.mul(flagInstance.transform)

        return boundingBox.getCenter(temp).dst(temp) / Math.max(boundingBox.width, boundingBox.depth) / 2
    }

    fun updateBackendData(newData: BaseData) {
        backendData = newData

        geoPoint = backendData.coordinates!!.toGeoPoint()
    }
}