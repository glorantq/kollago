package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer

class LoadingScreen(private val performer: LoadingPerformer, private val onLoadComplete: () -> Unit) : MenuScreen() {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer

    override fun show() {
        super.show()

        performer.loadingProgress = 0
        performer.done = false
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        if(performer.done) {
            performer.done = false
            performer.loadingProgress = 0

            onLoadComplete()
            return
        }

        performer.performLoading()

        textRenderer.drawCenteredText("${performer.action} ${performer.loadingProgress}%", viewport.worldWidth / 2, 32f, 32, "Roboto", FontStyle.NORMAL, Color.WHITE)
    }

    interface LoadingPerformer {
        val action: String
        var loadingProgress: Int
        var done: Boolean

        fun performLoading()
    }
}