package skacce.rs.kollago.screens

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import ktx.math.vec2
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.gui.elements.GuiButton
import skacce.rs.kollago.gui.elements.GuiTextInput
import skacce.rs.kollago.input.text.TextInputProvider

class EmailRegisterScreen : MenuScreen() {
    private lateinit var passwordInput: GuiTextInput
    private lateinit var passwordConfirmationInput: GuiTextInput
    private lateinit var emailInput: GuiTextInput
    private lateinit var registerButton: GuiButton

    override fun show() {
        super.show()

        val inputSize: Vector2 = vec2(300f, 90f)

        passwordInput = GuiTextInput(viewport.worldWidth / 2 - (inputSize.x + 40f) / 2, viewport.worldHeight / 2 - inputSize.y / 2, inputSize.x + 40f, inputSize.y, "Jelszó",
                TextInputProvider.InputType.PASSWORD, 64, this)
        passwordConfirmationInput = GuiTextInput(passwordInput.x, passwordInput.y - inputSize.y - 10f, passwordInput.width, passwordInput.height, "Jelszó megerősítése", TextInputProvider.InputType.PASSWORD, 64, this)
        emailInput = GuiTextInput(passwordInput.x, passwordInput.y + inputSize.y + 10f, passwordInput.width, passwordInput.height, "E-Mail", TextInputProvider.InputType.TEXT, 64, this)
        registerButton = GuiButton(viewport.worldWidth / 2 - (inputSize.x - 40f) / 2, passwordConfirmationInput.y - inputSize.y - 10f, inputSize.x - 40f, inputSize.y, "Regisztráció", GuiButton.Style())

        val loginButton: GuiButton = GuiButton(registerButton.x, registerButton.y - inputSize.y * 2, registerButton.width, registerButton.height, "Bejelentkezés", GuiButton.Style())
        val backButton: GuiButton = GuiButton(10f, 10f, 150f, 75f, "Vissza", GuiButton.Style())

        createElement(emailInput)
        createElement(passwordInput)
        createElement(passwordConfirmationInput)
        createElement(registerButton)
        createElement(loginButton)
        createElement(backButton)

        registerButton.clickHandler = {
            KollaGO.INSTANCE.platform.performEmailAuth(emailInput.text, passwordInput.text, true) { success, message ->
                if(success) {
                    KollaGO.INSTANCE.networkManager.beginLoginSequence()
                } else {
                    val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
                    dialog.setTitle("Regisztráció")
                    dialog.setMessage(message)
                    dialog.addButton("OK")
                    dialog.setCancelable(true)

                    dialog.build().show()
                }
            }
        }

        backButton.clickHandler = {
            KollaGO.INSTANCE.screen = LoginScreen()
        }

        loginButton.clickHandler = {
            KollaGO.INSTANCE.screen = EmailLoginScreen()
        }
    }

    override fun draw(spriteBatch: SpriteBatch) {
        super.draw(spriteBatch)

        drawElements(spriteBatch)

        registerButton.enabled = emailInput.text.isNotEmpty() && passwordInput.text.length >= 6 && passwordInput.text == passwordConfirmationInput.text
    }
}