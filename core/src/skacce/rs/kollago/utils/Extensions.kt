package skacce.rs.kollago.utils

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import org.oscim.core.GeoPoint
import skacce.rs.kollago.network.protocol.Coordinates

fun Model.newInstance(): ModelInstance = ModelInstance(this)
fun Coordinates.toGeoPoint(): GeoPoint = GeoPoint(this.latitude.toDouble(), this.longitude.toDouble())
fun GeoPoint.toCoordinates(): Coordinates = Coordinates(this.latitude.toFloat(), this.longitude.toFloat())