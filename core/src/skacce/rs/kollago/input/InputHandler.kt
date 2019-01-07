package skacce.rs.kollago.input

interface InputHandler {
    fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    fun keyDown(key: Int): Boolean {
        return false
    }

    fun keyUp(key: Int): Boolean {
        return false
    }
}
