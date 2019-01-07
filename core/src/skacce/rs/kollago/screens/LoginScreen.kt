package skacce.rs.kollago.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.elements.GuiButton

class LoginScreen : MenuScreen() {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer
    private var uid: String = ""

    override fun show() {
        super.show()

        val googleLoginButton: GuiButton = GuiButton(viewport.worldWidth / 2f - 150f, viewport.worldHeight / 2f + 10f, 300f, 90f, "Google", GuiButton.Style())
        val emailLoginButton: GuiButton = GuiButton(googleLoginButton.x, viewport.worldHeight / 2f - 95f, 300f, 90f, "E-Mail", GuiButton.Style())
        val a: GuiButton = GuiButton(googleLoginButton.x, viewport.worldHeight / 2f - 95f - 100f, 300f, 90f, "lakker", GuiButton.Style())

        googleLoginButton.clickHandler = {_ ->
            KollaGO.INSTANCE.platform.performGoogleLogIn {success ->
                if(success) {
                    KollaGO.INSTANCE.platform.getFirebaseUID {token ->
                        uid = token
                    }
                }
            }
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
            textRenderer.drawCenteredText(KollaGO.INSTANCE.platform.getUser().getDisplayName(), viewport.worldWidth / 2 , 100f, 30, "Roboto", FontStyle.NORMAL, Color.WHITE)
            textRenderer.drawCenteredText(KollaGO.INSTANCE.platform.getUser().getEMail(), viewport.worldWidth / 2 , 130f, 30, "Roboto", FontStyle.NORMAL, Color.WHITE)
            textRenderer.drawCenteredText(uid, viewport.worldWidth / 2 , 160f, 30, "Roboto", FontStyle.NORMAL, Color.WHITE)
        }

        textRenderer.drawCenteredText("Copyright (c) 2019 Appropati", viewport.worldWidth / 2, 20f, 24, "Roboto", FontStyle.NORMAL, Color.WHITE)
    }
}