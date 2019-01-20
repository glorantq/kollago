package skacce.rs.kollago.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.elements.GuiButton

class ConfirmEmailScreen : MenuScreen() {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer

    private lateinit var resendButton: GuiButton
    private var resendTimeout: Float = 0f

    override fun show() {
        super.show()

        val inputSize: Vector2 = vec2(300f, 90f)

        resendButton = GuiButton(viewport.worldWidth / 2 - inputSize.x - 5, viewport.worldHeight / 4 - inputSize.y * 2, inputSize.x, inputSize.y, "Újraküldés", GuiButton.Style())

        val retryButton: GuiButton = GuiButton(viewport.worldWidth / 2 + 5, resendButton.y, resendButton.width, resendButton.height, "Újrapróbálás", GuiButton.Style())
        val backButton: GuiButton = GuiButton(10f, 10f, 150f, 75f, "Vissza", GuiButton.Style())

        createElement(resendButton)
        createElement(retryButton)
        createElement(backButton)

        backButton.clickHandler = {
            KollaGO.INSTANCE.screen = LoginScreen()
        }

        resendButton.clickHandler = {
            KollaGO.INSTANCE.platform.getUser().sendVerificationEmail()
            resendTimeout = 30f
        }

        retryButton.clickHandler = {
            retryButton.enabled = false

            KollaGO.INSTANCE.platform.getUser().refresh { success, message ->
                if (!success) {
                    showDialog("Profil Frissítése", message)
                } else {
                    if(!KollaGO.INSTANCE.platform.getUser().isEmailVerified()) {
                        showDialog("Megerősítés", "Nincs megerősítve a cím!")
                    } else {
                        KollaGO.INSTANCE.networkManager.beginLoginSequence()
                    }
                }

                retryButton.enabled = true
            }
        }
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        drawElements(spriteBatch)

        textRenderer.drawCenteredText("E-Mail cím megerősítése", viewport.worldWidth / 2, viewport.worldHeight - viewport.worldHeight / 4, 46, "Roboto", FontStyle.BOLD, Color.WHITE)
        textRenderer.drawWrappedText("A folytatáshoz meg kell erősítened e-mail címedet. Regisztrációkor elküldtünk egy visszaigazoló linket, de amennyiben nem kaptad meg, itt újat igényelhetsz.", 20f, viewport.worldHeight - viewport.worldHeight / 4 - 46, 35, "Roboto", FontStyle.NORMAL, Color.WHITE, viewport.worldWidth - 40, Align.center)
        textRenderer.drawCenteredText(KollaGO.INSTANCE.platform.getUser().getEMail(), viewport.worldWidth / 2, resendButton.y + resendButton.height + 30, 30, "Roboto", FontStyle.ITALIC, Color.LIGHT_GRAY)

        resendButton.enabled = resendTimeout <= 0
        if (resendTimeout > 0) {
            resendTimeout -= Gdx.graphics.deltaTime

            resendButton.text = resendTimeout.toInt().toString()
        } else {
            resendButton.text = "Újraküldés"
        }
    }

    private fun showDialog(title: String, message: String) {
        val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.addButton("OK")
        dialog.setCancelable(true)

        dialog.build().show()
    }
}