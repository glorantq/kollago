package skacce.rs.kollago.utils

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector2
import ktx.math.vec2
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

fun Texture.fit(target: Vector2): Vector2 {
    val widthRatio = target.x / this.width
    val heightRatio = target.y / this.height
    val ratio = Math.max(widthRatio, heightRatio)

    return vec2(ratio * this.width, ratio * this.height)
}

fun Texture.scaleToWidth(target: Float): Vector2 {
    return vec2(target, target * this.height / this.width)
}

fun Texture.scaleToHeight(target: Float): Vector2 {
    return vec2(target * this.width / this.height, target)
}

fun SpriteBatch.draw(texture: Texture, pos: Vector2, size: Float) {
    this.draw(texture, pos.x, pos.y, size, size)
}

fun SpriteBatch.draw(texture: Texture, x: Float, y: Float, size: Vector2) {
    this.draw(texture, x, y, size.x, size.y)
}

fun SpriteBatch.draw(texture: Texture, pos: Vector2, size: Vector2) {
    this.draw(texture, pos.x, pos.y, size.x, size.y)
}