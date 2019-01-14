package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.elements.GuiButton

class LoginScreen : MenuScreen() {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer

    override fun show() {
        super.show()

        val googleLoginButton: GuiButton = GuiButton(viewport.worldWidth / 2f - 150f, viewport.worldHeight / 2f + 10f, 300f, 90f, "Google", GuiButton.Style())
        val emailLoginButton: GuiButton = GuiButton(googleLoginButton.x, viewport.worldHeight / 2f - 95f, 300f, 90f, "E-Mail", GuiButton.Style())

        googleLoginButton.clickHandler = {
            KollaGO.INSTANCE.platform.performGoogleLogIn { success, message ->
                if(success) {
                   KollaGO.INSTANCE.networkManager.beginLoginSequence()
                } else {
                    val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
                    dialog.setTitle("Bejelentkezés")
                    dialog.setMessage(message)
                    dialog.addButton("OK")
                    dialog.setCancelable(true)

                    dialog.build().show()
                }
            }
        }

        emailLoginButton.clickHandler = { _ ->
            KollaGO.INSTANCE.screen = EmailLoginScreen()
        }

        createElement(googleLoginButton)
        createElement(emailLoginButton)
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        drawElements(spriteBatch)

        textRenderer.drawCenteredText("Copyright (c) 2019 Appropati", viewport.worldWidth / 2, 20f, 24, "Roboto", FontStyle.NORMAL, Color.WHITE)
    }
}