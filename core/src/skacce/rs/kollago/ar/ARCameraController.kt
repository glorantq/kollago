package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import java.lang.reflect.Field

class ARCameraController(private val camera: Camera, private val maxPitch: Int, private val minPitch: Int) : GestureDetector(null) {
    private val target = Vector3()

    private var button = -1
    private var touched: Int = 0
    private var multiTouch: Boolean = false

    private var startX: Float = 0.toFloat()
    private var startY:Float = 0.toFloat()
    private val tmpV1 = Vector3()

    private var rotatingY = false
    private var lastTapTime = 0L
    private val lastTapLocation = Vector2()

    private inner class CameraGestureListener : GestureDetector.GestureAdapter() {
        var cameraController: ARCameraController? = null

        override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
            cameraController!!.lastTapTime = System.currentTimeMillis()
            cameraController!!.lastTapLocation.set(x, y)

            return super.tap(x, y, count, button)
        }
    }

    init {
        val field: Field = GestureDetector::class.java.getDeclaredField("listener")
        field.isAccessible = true

        val gestureListener: CameraGestureListener = CameraGestureListener()
        gestureListener.cameraController = this

        field.set(this, gestureListener)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        touched = touched or (1 shl pointer)
        multiTouch = !MathUtils.isPowerOfTwo(touched)

        if (multiTouch) {
            this.button = -1
        } else if (this.button < 0) {
            startX = screenX.toFloat()
            startY = screenY.toFloat()

            this.button = button
        }

        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        touched = touched and (-1 xor (1 shl pointer))
        multiTouch = !MathUtils.isPowerOfTwo(touched)

        if (button == this.button) {
            this.button = -1
        }

        rotatingY = false

        return super.touchUp(screenX, screenY, pointer, button)
    }

    private fun process(deltaX: Float, deltaY: Float, button: Int, rotateY: Boolean): Boolean {
        if (button == Input.Buttons.LEFT) {
            tmpV1.set(camera.direction).crs(camera.up).y = 0f

            if (rotateY) {
                camera.rotateAround(target, tmpV1.cpy().nor(), deltaY * 360)
                rotatingY = true
            } else {
                camera.rotateAround(target, Vector3.Y, deltaX * -360)
            }

            val cameraDirection = camera.direction

            val pitch = (Math.atan2(Math.sqrt((cameraDirection.x * cameraDirection.x + cameraDirection.z * cameraDirection.z).toDouble()), cameraDirection.y.toDouble()) * MathUtils.radiansToDegrees).toFloat()

            tmpV1.nor()
            if (pitch < minPitch) {
                camera.rotateAround(Vector3.Zero, tmpV1, pitch - minPitch)
            }

            if (pitch > maxPitch) {
                camera.rotateAround(Vector3.Zero, tmpV1, pitch - maxPitch)
            }
        }

        camera.update()

        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val result = super.touchDragged(screenX, screenY, pointer)

        if (result || this.button < 0) {
            return result
        }

        val deltaX = (screenX - startX) / Gdx.graphics.width
        val deltaY = (startY - screenY) / Gdx.graphics.height

        startX = screenX.toFloat()
        startY = screenY.toFloat()

        return process(deltaX, deltaY, button, System.currentTimeMillis() - lastTapTime <= 500 && lastTapLocation.dst(screenX.toFloat(), screenY.toFloat()) < 64 || rotatingY)
    }
}