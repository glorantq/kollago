package skacce.rs.kollago.utils

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.utils.UBJsonReader
import skacce.rs.kollago.KollaGO

object ARUtils {
    private val modelLoader: G3dModelLoader = G3dModelLoader(UBJsonReader())

    fun createPlaneMesh(width: Float, height: Float, u1: Float, v1: Float, u2: Float, v2: Float): Mesh {
        val meshBuilder = MeshBuilder()
        meshBuilder.begin((VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong())

        meshBuilder.part("rect", GL20.GL_TRIANGLES)

        meshBuilder.setUVRange(u1, v1, u2, v2)
        meshBuilder.rect(
                -(width * 0.5f), 0f, -(height * 0.5f),
                width * 0.5f, 0f, -(height * 0.5f),
                width * 0.5f, 0f, height * 0.5f,
                -(width * 0.5f), 0f, height * 0.5f,
                0f, 0f, -1f)

        return meshBuilder.end()
    }

    fun createSkySphere(): Model {
        val sphereTexture = KollaGO.INSTANCE.textureManager.getTexture("sky/sky.png")

        val material = Material()
        material.set(TextureAttribute.createDiffuse(sphereTexture))
        material.set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f))

        return ModelBuilder().createSphere(KollaGO.MAP_RESOLUTION, KollaGO.MAP_RESOLUTION, KollaGO.MAP_RESOLUTION, 16, 16, material, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong())
    }

    fun loadExternalModel(modelFile: FileHandle): Model {
        val model: Model = modelLoader.loadModel(modelFile) { KollaGO.INSTANCE.textureManager[it] }

        model.materials.forEach {
            it.set(ColorAttribute(ColorAttribute.Emissive, 0f, 0f, 0f, 0f))
            it.set(ColorAttribute(ColorAttribute.Specular, 0f, 0f, 0f, 0f))
            it.set(FloatAttribute(FloatAttribute.Shininess, 0f))
        }

        return model
    }
}