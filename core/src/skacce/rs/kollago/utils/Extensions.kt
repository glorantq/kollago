package skacce.rs.kollago.utils

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import org.oscim.core.GeoPoint
import skacce.rs.kollago.network.protocol.Coordinates
import skacce.rs.kollago.network.protocol.ProfileData

fun Model.newInstance(): ModelInstance = ModelInstance(this)
fun Coordinates.toGeoPoint(): GeoPoint = GeoPoint(this.latitude.toDouble(), this.longitude.toDouble())
fun GeoPoint.toCoordinates(): Coordinates = Coordinates(this.latitude.toFloat(), this.longitude.toFloat())

// level=3ln(x+1)+1
fun ProfileData.levelRaw(): Float = ((Math.log(xp.toDouble() / 1000.0 + 1.0).toFloat() * 3f) + 1f)
fun ProfileData.level(): Float = Math.round(levelRaw() * 10f) / 10f
fun ProfileData.levelXp(level: Float): Int {
    // 3ln(x+1)+1=level
    var levelCalc: Double = level.toDouble()

    // 3ln(x+1)=level-1
    levelCalc--

    // ln(x+1)=level/3
    levelCalc /= 3.0

    // x+1=e^level
    val result: Double = Math.exp(levelCalc)

    // x=result-1

    return ((result - 1.0) * 1000.0).toInt()
}
fun ProfileData.xpToNextLevel(): Int = (levelXp(level() + 1) - xp).toInt()
fun ProfileData.levelProgress(): Float {
    val level: Float = level()
    val rawLevel: Float = levelRaw()

    return if(level.toInt() > rawLevel.toInt()) {
        level - level.toInt()
    } else {
        rawLevel - rawLevel.toInt()
    }
}