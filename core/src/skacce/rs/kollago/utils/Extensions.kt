package skacce.rs.kollago.utils

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance

fun Model.newInstance(): ModelInstance = ModelInstance(this)