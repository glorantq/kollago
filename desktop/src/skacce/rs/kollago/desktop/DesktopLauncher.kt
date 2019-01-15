package skacce.rs.kollago.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.utils.SharedLibraryLoader

import org.oscim.awt.AwtGraphics
import org.oscim.backend.GLAdapter
import org.oscim.core.GeoPoint
import org.oscim.core.Tile
import org.oscim.gdx.GdxAssets
import org.oscim.utils.FastMath

import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.Platform
import skacce.rs.kollago.input.text.TextInputListener
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.map.GdxGL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

class DesktopLauncher : Platform, TextInputProvider {
    private val textInputListeners = CopyOnWriteArrayList<TextInputListener>()

    override fun getGpsPosition(): GeoPoint = GeoPoint(47.199968, 18.439003)

    override fun initGps(): CompletableFuture<Boolean> = CompletableFuture.completedFuture(true)

    override fun checkGpsState(): Boolean = true

    override fun getSafeAreaOffset(): Int = 0

    override fun registerListener(textInputListener: TextInputListener) {
        textInputListeners.add(textInputListener)
    }

    override fun removeListener(textInputListener: TextInputListener) {
        textInputListeners.remove(textInputListener)
    }

    override fun openTextInput(placeholder: String, text: String, type: TextInputProvider.InputType, maxChars: Int) {
        Gdx.input.getTextInput(object : Input.TextInputListener {
            override fun input(s: String) {
                textInputListeners.forEach { it -> it.textUpdated(s) }
            }

            override fun canceled() {

            }
        }, placeholder, text, placeholder)
    }

    override fun closeTextInput() {

    }

    override fun isLoggedIn(): Boolean {
        return false
    }

    override fun getFirebaseUID(callback: (token: String) -> Unit) {
        callback("")
    }

    override fun performGoogleLogIn(callback: (success: Boolean, message: String) -> Unit) {
        callback(false, "")
    }

    override fun performEmailAuth(email: String, password: String, register: Boolean, callback: (success: Boolean, message: String) -> Unit) {
        callback(false, "")
    }

    override fun logOut(callback: () -> Unit) {
        callback()
    }

    override fun isEMailVerified(uid: String): Boolean {
        return false
    }

    override fun getUser(): Platform.NativeAuthUser {
        return DesktopUser()
    }

    override fun backupGeocode(position: GeoPoint): String {
        return ""
    }

    class DesktopUser : Platform.NativeAuthUser {
        override fun getEMail(): String {
            return ""
        }

        override fun getDisplayName(): String {
            return ""
        }

        override fun isEmailVerified(): Boolean {
            return false
        }

        override fun refresh(callback: (success: Boolean, message: String) -> Unit) {
            callback(false, "")
        }

        override fun sendVerificationEmail() {

        }
    }
}

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()

    SharedLibraryLoader().load("vtm-jni")
    AwtGraphics.init()

    GdxAssets.init("assets/")
    GLAdapter.init(GdxGL())

    Tile.SIZE = FastMath.clamp(Tile.SIZE, 128, 512)
    config.title = "Kolla GO"
    config.resizable = true
    config.stencil = 16
    config.samples = 2
    config.width = 554
    config.height = 985

    val launcher: DesktopLauncher = DesktopLauncher()
    LwjglApplication(KollaGO.create(launcher, launcher), config)
}