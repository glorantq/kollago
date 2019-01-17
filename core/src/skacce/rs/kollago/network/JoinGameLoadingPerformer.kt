package skacce.rs.kollago.network

import com.badlogic.gdx.Gdx
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.screens.LoadingScreen
import skacce.rs.kollago.screens.LoginScreen
import kotlin.concurrent.thread

class JoinGameLoadingPerformer : LoadingScreen.LoadingPerformer {
    override val action: String = "Csatlakozás a játékhoz"
    override var loadingProgress: Int = 0
    override var done: Boolean = false

    init {
        thread {
            KollaGO.INSTANCE.networkManager.joinGameServer {
                if(!it) {
                    failWithDialog("Csatlakozás", "Nem sikerült csatlakozni a játékhoz!")
                    return@joinGameServer
                }

                loadingProgress = 100
                done = true
            }
        }
    }

    override fun performLoading() {

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
}