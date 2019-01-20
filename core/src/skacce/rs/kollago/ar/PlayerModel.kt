package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import skacce.rs.kollago.utils.ARUtils
import skacce.rs.kollago.utils.newInstance

class PlayerModel(val name: String, private val idleAnimation: String, private val runningAnimation: String) {
    private val animationController: AnimationController
    val modelInstance: ModelInstance

    init {
        val model: Model = ARUtils.loadExternalModel(Gdx.files.internal("player/$name/$name.g3db"))
        modelInstance = model.newInstance()

        animationController = AnimationController(modelInstance)
        animationController.setAnimation(idleAnimation, -1)
    }

    fun render(modelBatch: ModelBatch) {
        animationController.update(Gdx.graphics.deltaTime)
        modelBatch.render(modelInstance)
    }

    fun isRunning(): Boolean = animationController.current.animation.id.equals(runningAnimation, true)

    fun setRunningAnimation() {
        animationController.setAnimation(runningAnimation, -1)
    }

    fun setIdleAnimation() {
        animationController.setAnimation(idleAnimation, -1)
    }
}