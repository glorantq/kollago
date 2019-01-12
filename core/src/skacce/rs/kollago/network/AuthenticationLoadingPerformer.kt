package skacce.rs.kollago.network

import com.badlogic.gdx.Gdx
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.Platform
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
                            loadingProgress = 100
                            done = true
                        }
                    }
                } else {
                    Gdx.app.postRunnable {
                        platform.logOut {
                            KollaGO.INSTANCE.screen = LoginScreen()
                        }

                        val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
                        dialog.setTitle("Fiók Frissítése")
                        dialog.setMessage(message)
                        dialog.addButton("OK")
                        dialog.setCancelable(true)

                        dialog.build().show()
                    }
                }
            }
        }
    }

    override fun performLoading() {

    }
}