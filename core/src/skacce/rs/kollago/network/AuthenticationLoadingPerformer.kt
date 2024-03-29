package skacce.rs.kollago.network

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.Platform
import skacce.rs.kollago.screens.ConfirmEmailScreen
import skacce.rs.kollago.screens.LoadingScreen
import skacce.rs.kollago.screens.LoginScreen
import skacce.rs.kollago.screens.RegistrationScreen
import kotlin.concurrent.thread

class AuthenticationLoadingPerformer : LoadingScreen.LoadingPerformer {
    override val action: String = "Bejelentkezés"
    override var loadingProgress: Int = 0
    override var done: Boolean = false

    init {
        thread {
            val platform: Platform = KollaGO.INSTANCE.platform

            Gdx.app.log("Authentication", "Starting config refresh")

            platform.updateRemoteConfig {
                if (!it) {
                    failWithDialog("Firebase", "Nem elérhető A Firebase szerver!")
                    return@updateRemoteConfig
                } else {
                    Gdx.app.log("Authentication", "Refreshed config, checking GPS...")

                    if (!platform.checkGpsState()) {
                        Gdx.app.log("Authentication", "Starting GPS init")
                        platform.initGps().thenAccept {
                            if (it) {
                                Gdx.app.log("Authentication", "GPS init done")
                                loadingProgress = 20
                                continueLoginGPS()
                            } else {
                                Gdx.app.error("Authentication", "Failed to init GPS")
                                failWithDialog("GPS", "Nem elérhető a GPS! Jobb hibaüzenet bitte")
                            }
                        }
                    } else {
                        continueLoginGPS()
                    }
                }
            }
        }
    }

    private fun continueLoginGPS() {
        val platform: Platform = KollaGO.INSTANCE.platform

        Gdx.app.log("Authentication", "Starting login")

        if (!platform.isLoggedIn()) {
            KollaGO.INSTANCE.screen = LoginScreen()
            return
        }

        loadingProgress = 40

        val user: Platform.NativeAuthUser = platform.getUser()

        user.refresh { success, message ->
            if (success) {
                if (!user.isEmailVerified()) {
                    setScreen(ConfirmEmailScreen())

                    return@refresh
                }

                loadingProgress = 60

                platform.getFirebaseUID {
                    if (it.isBlank()) {
                        Gdx.app.postRunnable {
                            platform.logOut {
                                KollaGO.INSTANCE.screen = LoginScreen()
                            }
                        }
                    } else {
                        loadingProgress = 80

                        val currentPosition: GeoPoint = platform.getGpsPosition()
                        KollaGO.INSTANCE.networkManager.tryApiLogin(it, currentPosition) { errorCode, message ->
                            handleLoginResponse(errorCode, it, message)
                        }
                    }
                }
            } else {
                failWithDialog("Fiók Frissítése", message)
            }
        }
    }

    private fun handleLoginResponse(code: Int, uid: String, message: String) {
        when (code) {
            -1 -> failWithDialog("KollaGO Szerver", "Nem lehet elérni a központi szervert!")
            1 -> failWithDialog("Bejelentkezés", "Szerverhiba")
            3 -> failWithDialog("Bejelentkezés", "Hitelesítési hiba")
            4 -> failWithDialog("Bejelentkezés", "Hibás UID")
            5 -> failWithDialog("Bejelentkezés", "Hibás pozíció")

            6 -> setScreen(ConfirmEmailScreen())
            7 -> setScreen(RegistrationScreen(uid))

            0 -> {
                val jsonRoot: JSONObject = JSONParser().parse(message) as JSONObject
                KollaGO.INSTANCE.networkManager.nominatimAddress = jsonRoot["nominatim"].toString()
                KollaGO.INSTANCE.networkManager.gameServerAddress = jsonRoot["game_server"].toString()
                KollaGO.INSTANCE.networkManager.firebaseUid = uid

                loadingProgress = 100
                done = true
            }
        }
    }

    private fun failWithDialog(title: String, message: String) {
        Gdx.app.postRunnable {
            KollaGO.INSTANCE.platform.logOut {
                KollaGO.INSTANCE.screen = LoginScreen()
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

    override fun performLoading() {

    }
}