package skacce.rs.kollago.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.graphics.text.FontStyle
import skacce.rs.kollago.graphics.text.TextRenderer
import skacce.rs.kollago.gui.elements.GuiButton
import skacce.rs.kollago.gui.elements.GuiTextInput
import skacce.rs.kollago.input.InputHandler
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.network.protocol.ProfileData

class RegistrationScreen(private val uid: String) : MenuScreen(), InputHandler {
    private val textRenderer: TextRenderer = KollaGO.INSTANCE.textRenderer
    private val safeNameRegex: Regex = Regex("[a-zA-Z0-9_]+")

    private val position: GeoPoint = KollaGO.INSTANCE.platform.getGpsPosition()
    private val currentAddress: String = KollaGO.INSTANCE.platform.backupGeocode(position)

    private lateinit var doneButton: GuiButton
    private lateinit var usernameInput: GuiTextInput

    private lateinit var models: List<ModelButton>
    private var selectedModel: Int = 0
    private lateinit var outlineTexture: Texture

    override fun show() {
        super.show()

        KollaGO.INSTANCE.inputHandler.addInputHandler(this)

        models = arrayListOf(
                ModelButton(ProfileData.PlayerModel.DEKU, viewport.worldWidth / 2 - 256f - 10f, viewport.worldHeight / 2 - 128f),
                ModelButton(ProfileData.PlayerModel.URARAKA, viewport.worldWidth / 2 + 10f, viewport.worldHeight / 2 - 128f)
        )

        usernameInput = GuiTextInput(viewport.worldWidth / 2 - 150, viewport.worldHeight / 2 + 256f + 20f, 300f, 90f, "Felhasználónév",
                TextInputProvider.InputType.TEXT, 16, this)

        outlineTexture = KollaGO.INSTANCE.textureManager["portrait/outline.png"]

        val backButton: GuiButton = GuiButton(10f, 10f, 150f, 75f, "Vissza", GuiButton.Style())
        doneButton = GuiButton(viewport.worldWidth - 150f - 10f, 10f, 150f, 75f, "Tovább", GuiButton.Style())

        createElement(usernameInput)
        createElement(backButton)
        createElement(doneButton)

        backButton.clickHandler = {
            KollaGO.INSTANCE.platform.logOut {
                KollaGO.INSTANCE.screen = LoginScreen()
            }
        }

        doneButton.clickHandler = {
            KollaGO.INSTANCE.networkManager.tryApiRegister(uid, usernameInput.text, ProfileData.PlayerModel.fromValue(selectedModel), position) { code, message ->
                when(code) {
                    -1 -> showDialog("KollaGO Szerver", "Nem lehet elérni a központi szervert!")
                    1 -> showDialog("Regisztráció", "Szerverhiba")
                    3 -> showDialog("Regisztráció", "Hitelesítési hiba: $message")
                    4 -> showDialog("Regisztráció", "Hibás UID")
                    5 -> showDialog("Regisztráció", "Hibás pozíció", false)

                    6 -> setScreen(ConfirmEmailScreen())

                    7 -> showDialog("Regisztráció", "A név érvénytelen karaktereket tartalmaz!", false)
                    8 -> showDialog("Regisztráció", "Hibás karakter", false)
                    9 -> showDialog("Regisztráció", "Ez a felhasználónév már foglalt!", false)
                    10 -> showDialog("Registráció", "Adatbázishiba")

                    0 -> KollaGO.INSTANCE.networkManager.beginLoginSequence()
                }
            }
        }
    }

    override fun draw(spriteBatch: SpriteBatch) {
        doneButton.enabled = usernameInput.text.isNotBlank() && usernameInput.text.matches(safeNameRegex)

        super.draw(spriteBatch)
        drawElements(spriteBatch)

        textRenderer.drawCenteredText("Profil Készítése", viewport.worldWidth / 2, usernameInput.y + usernameInput.height + 40f, 46, "Roboto", FontStyle.BOLD, Color.WHITE)
        textRenderer.drawWrappedText("A felhasználónév nem tartalmazhat ékezetes betűket.", 40f, usernameInput.y - 30f, 30, "Roboto", FontStyle.ITALIC, Color.LIGHT_GRAY,
                viewport.worldWidth - 80f, Align.center)
        textRenderer.drawCenteredText("A bázisod a jelenlegi pozíciódban lesz létrehozva:", viewport.worldWidth / 2, viewport.worldHeight / 2 - 128f - 60f, 30, "Roboto",
                FontStyle.ITALIC, Color.LIGHT_GRAY)
        textRenderer.drawWrappedText(currentAddress, 40f, viewport.worldHeight / 2 - 128f - 90f, 30, "Roboto", FontStyle.ITALIC,
                Color.LIGHT_GRAY, viewport.worldWidth - 80f, Align.center)

        for(button: ModelButton in models) {
            if(button.model.value == selectedModel) {
                spriteBatch.setColor(1f, 1f, 1f, 1f)

                spriteBatch.draw(outlineTexture, button.x - 10f, button.y - 10f)
            } else {
                spriteBatch.setColor(.5f, .5f, .5f, 1f)
            }

            spriteBatch.draw(button.texture, button.x, button.y, 256f, 256f)
        }

        spriteBatch.setColor(1f, 1f, 1f, 1f)
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        models.forEach {
            if(it.bb.contains(x, y)) {
                selectedModel = it.model.value

                return true
            }
        }

        return false
    }

    private fun showDialog(title: String, message: String, quit: Boolean = true) {
        Gdx.app.postRunnable {
            if(quit) {
                KollaGO.INSTANCE.platform.logOut {
                    KollaGO.INSTANCE.screen = LoginScreen()
                }
            }

            val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.addButton("OK")
            dialog.setCancelable(true)

            dialog.build().show()
        }
    }

    private fun setScreen(screen: Screen) {
        Gdx.app.postRunnable {
            KollaGO.INSTANCE.screen = screen
        }
    }

    override fun hide() {
        super.hide()

        KollaGO.INSTANCE.inputHandler.removeInputHandler(this)
    }

    private class ModelButton(val model: ProfileData.PlayerModel, val x: Float, val y: Float) {
        val texture: Texture = KollaGO.INSTANCE.textureManager["portrait/${model.value}.png"]
        val bb: Rectangle = Rectangle(x, y, 256f, 256f)
    }
}