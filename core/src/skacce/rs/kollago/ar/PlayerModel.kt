package skacce.rs.kollago.ar

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import skacce.rs.kollago.utils.ARUtils
import skacce.rs.kollago.utils.newInstance

class PlayerModel(val name: String, val idleAnimation: String, val runningAnimation: String) {
    val modelInstance: ModelInstance

    init {
        val model: Model = ARUtils.loadExternalModel(Gdx.files.internal("player/$name/$name.g3db"))
        modelInstance = model.newInstance()
    }
}