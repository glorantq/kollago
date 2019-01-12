package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.elements.GuiButton
import skacce.rs.kollago.network.AuthenticationLoadingPerformer

class LoginScreen : MenuScreen() {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer
    private var uid: String = ""

    override fun show() {
        super.show()

        val googleLoginButton: GuiButton = GuiButton(viewport.worldWidth / 2f - 150f, viewport.worldHeight / 2f + 10f, 300f, 90f, "Google", GuiButton.Style())
        val emailLoginButton: GuiButton = GuiButton(googleLoginButton.x, viewport.worldHeight / 2f - 95f, 300f, 90f, "E-Mail", GuiButton.Style())
        val a: GuiButton = GuiButton(googleLoginButton.x, viewport.worldHeight / 2f - 95f - 100f, 300f, 90f, "lakker", GuiButton.Style())

        googleLoginButton.clickHandler = {
            KollaGO.INSTANCE.platform.performGoogleLogIn { success, message ->
                if(success) {
                    KollaGO.INSTANCE.screen = LoadingScreen(AuthenticationLoadingPerformer()) {
                        KollaGO.INSTANCE.screen = ARWorld()
                    } // TODO
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

        a.clickHandler = {
            KollaGO.INSTANCE.platform.logOut {  }
        }

        createElement(googleLoginButton)
        createElement(emailLoginButton)
        createElement(a)
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        drawElements(spriteBatch)

        if(KollaGO.INSTANCE.platform.isLoggedIn()) {
            textRenderer.drawCenteredText("Name: ${KollaGO.INSTANCE.platform.getUser().getDisplayName()}", viewport.worldWidth / 2 , 100f, 30, "Roboto", FontStyle.NORMAL, Color.RED)
            textRenderer.drawCenteredText("E-Mail: ${KollaGO.INSTANCE.platform.getUser().getEMail()} (Verified: ${KollaGO.INSTANCE.platform.getUser().isEmailVerified()})", viewport.worldWidth / 2 , 130f, 30, "Roboto", FontStyle.NORMAL, Color.RED)
            textRenderer.drawCenteredText(uid, viewport.worldWidth / 2 , 160f, 30, "Roboto", FontStyle.NORMAL, Color.RED)
        }

        textRenderer.drawCenteredText("Copyright (c) 2019 Appropati", viewport.worldWidth / 2, 20f, 24, "Roboto", FontStyle.NORMAL, Color.WHITE)
    }
}