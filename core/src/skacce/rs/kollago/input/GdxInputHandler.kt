package skacce.rs.kollago.input

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import skacce.rs.kollago.KollaGO
import java.util.concurrent.CopyOnWriteArrayList

class GdxInputHandler : GestureDetector.GestureListener, InputProcessor {
    private val viewport: Viewport = KollaGO.INSTANCE.staticViewport
    private val inputHandlers: MutableList<InputHandler> = CopyOnWriteArrayList<InputHandler>()

    fun addInputHandler(handler: InputHandler) {
        synchronized(inputHandlers) {
            inputHandlers.add(handler)
        }
    }

    fun removeInputHandler(handler: InputHandler) {
        synchronized(inputHandlers) {
            inputHandlers.remove(handler)
        }
    }

    override fun tap(v: Float, v1: Float, i: Int, i1: Int): Boolean {
        val coordinates: Vector3 = viewport.unproject(Vector3(v, v1, 0f))

        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.tap(coordinates.x, coordinates.y, i, i1)
            }
        }

        return false
    }

    override fun longPress(v: Float, v1: Float): Boolean {
        val coordinates: Vector3 = viewport.unproject(Vector3(v, v1, 0f))

        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.longPress(coordinates.x, coordinates.y)
            }
        }

        return false
    }

    override fun touchDown(i: Int, i1: Int, i2: Int, i3: Int): Boolean {
        val coordinates: Vector3 = viewport.unproject(Vector3(i.toFloat(), i1.toFloat(), 0f))

        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.touchDown(coordinates.x.toInt(), coordinates.y.toInt(), i2, i3)
            }
        }

        return false
    }

    override fun touchUp(i: Int, i1: Int, i2: Int, i3: Int): Boolean {
        val coordinates: Vector3 = viewport.unproject(Vector3(i.toFloat(), i1.toFloat(), 0f))

        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.touchUp(coordinates.x.toInt(), coordinates.y.toInt(), i2, i3)
            }
        }

        return false
    }

    override fun touchDown(v: Float, v1: Float, i: Int, i1: Int): Boolean {
        return false
    }

    override fun keyDown(i: Int): Boolean {
        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.keyDown(i)
            }
        }

        return false
    }

    override fun keyUp(i: Int): Boolean {
        synchronized(inputHandlers) {
            for (handler: InputHandler in inputHandlers) {
                handler.keyUp(i)
            }
        }

        return false
    }

    override fun keyTyped(c: Char): Boolean {
        return false
    }

    override fun touchDragged(i: Int, i1: Int, i2: Int): Boolean {
        return false
    }

    override fun mouseMoved(i: Int, i1: Int): Boolean {
        return false
    }

    override fun scrolled(i: Int): Boolean {
        return false
    }

    override fun fling(v: Float, v1: Float, i: Int): Boolean {
        return false
    }

    override fun pan(v: Float, v1: Float, v2: Float, v3: Float): Boolean {
        return false
    }

    override fun panStop(v: Float, v1: Float, i: Int, i1: Int): Boolean {
        return false
    }

    override fun zoom(v: Float, v1: Float): Boolean {
        return false
    }

    override fun pinch(vector2: Vector2, vector21: Vector2, vector22: Vector2, vector23: Vector2): Boolean {
        return false
    }

    override fun pinchStop() {

    }
}
