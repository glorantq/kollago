package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.gui.elements.GuiButton
import skacce.rs.kollago.gui.elements.GuiTextInput
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.network.AuthenticationLoadingPerformer

class EmailLoginScreen : MenuScreen() {
    private lateinit var passwordInput: GuiTextInput
    private lateinit var emailInput: GuiTextInput
    private lateinit var loginButton: GuiButton

    override fun show() {
        super.show()

        val inputSize: Vector2 = vec2(300f, 90f)

        passwordInput = GuiTextInput(viewport.worldWidth / 2 - (inputSize.x + 40f) / 2, viewport.worldHeight / 2 - inputSize.y / 2, inputSize.x + 40f, inputSize.y, "Jelszó",
                TextInputProvider.InputType.PASSWORD, 64, this)
        emailInput = GuiTextInput(passwordInput.x, passwordInput.y + inputSize.y + 10f, passwordInput.width, passwordInput.height, "E-Mail", TextInputProvider.InputType.TEXT, 64, this)
        loginButton = GuiButton(viewport.worldWidth / 2 - (inputSize.x - 40f) / 2, passwordInput.y - inputSize.y - 10f, inputSize.x - 40f, inputSize.y, "Bejelentkezés", GuiButton.Style())
        val registerButton: GuiButton = GuiButton(loginButton.x, loginButton.y - inputSize.y * 2, loginButton.width, loginButton.height, "Regisztráció", GuiButton.Style())

        val backButton: GuiButton = GuiButton(10f, 10f, 150f, 75f, "Vissza", GuiButton.Style())

        createElement(emailInput)
        createElement(passwordInput)
        createElement(loginButton)
        createElement(registerButton)
        createElement(backButton)

        backButton.clickHandler = {
            KollaGO.INSTANCE.screen = LoginScreen()
        }

        loginButton.clickHandler = {
            KollaGO.INSTANCE.platform.performEmailAuth(emailInput.text, passwordInput.text, false) { success, message ->
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

        registerButton.clickHandler = {
            KollaGO.INSTANCE.screen = EmailRegisterScreen()
        }
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        drawElements(spriteBatch)

        loginButton.enabled = emailInput.text.isNotEmpty() && passwordInput.text.isNotEmpty()
    }
}