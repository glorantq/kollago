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
import kotlin.concurrent.thread

class AuthenticationLoadingPerformer : LoadingScreen.LoadingPerformer {
    override val action: String = "Bejelentkezés"
    override var loadingProgress: Int = 0
    override var done: Boolean = false

    init {
        thread {
            val platform: Platform = KollaGO.INSTANCE.platform

            if (!platform.checkGpsState()) {
                val success: Boolean = platform.initGps().get()

                loadingProgress = 25

                if(!success) {
                    Gdx.app.exit()
                    return@thread
                }
            }

            if (!platform.isLoggedIn()) {
                KollaGO.INSTANCE.screen = LoginScreen()
                return@thread
            }

            loadingProgress = 50

            val user: Platform.NativeAuthUser = platform.getUser()

            user.refresh { success, message ->
                if(success) {
                    if (!user.isEmailVerified()) {
                        setScreen(ConfirmEmailScreen())

                        return@refresh
                    }

                    loadingProgress = 75

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
                            val request: JSONObject = JSONObject()
                            request["firebase_uid"] = it
                            request["current_lat"] = currentPosition.latitude
                            request["current_lon"] = currentPosition.longitude

                            KollaGO.INSTANCE.networkManager.performPost("http://192.168.1.108:8080/login", request.toJSONString()) { success, data ->
                                if(!success) {
                                    failWithDialog("Bejelentkezés", "Nem sikerült kapcsolódni a szerverhez!")
                                } else {
                                    loadingProgress = 85

                                    val response: JSONObject = JSONParser().parse(data) as JSONObject
                                    val errorCode: Int = response["error_code"].toString().toInt()

                                    handleLoginResponse(errorCode, it)
                                }
                            }
                        }
                    }
                } else {
                    failWithDialog("Fiók Frissítése", message)
                }
            }
        }
    }

    private fun handleLoginResponse(code: Int, uid: String) {
        when(code) {
            1 -> failWithDialog("Bejelentkezés", "Szerverhiba")
            3 -> failWithDialog("Bejelentkezés", "Hitelesítési hiba")
            4 -> failWithDialog("Bejelentkezés", "Hibás UID")
            5 -> failWithDialog("Bejelentkezés", "Hibás pozíció")

            6 -> setScreen(ConfirmEmailScreen())
            7 -> failWithDialog("Regisztráció", "nicht yet") // Registration needed TODO

            0 -> {
                loadingProgress = 100
                done = true
            } // Success TODO
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