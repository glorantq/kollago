package skacce.rs.kollago.network.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.Connection
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.network.protocol.AttackResult
import skacce.rs.kollago.network.protocol.NearFeaturesResponse
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.network.protocol.ProfileResponse

object GameplayNetworkHandler {
    val handleFeatureResponse: (Connection, NearFeaturesResponse, String) -> Unit = { _, packet, _ ->
        val screen: Screen = KollaGO.INSTANCE.screen

        if(screen is ARWorld) {
            Gdx.app.postRunnable {
                packet.stopData.forEach {
                    screen.createOrUpdateStop(it)
                }

                packet.baseData.forEach {
                    screen.createOrUpdateBase(it)
                }
            }
        }

        Gdx.app.log("GameplayHandler", "Created ${packet.stopData.size} stops and ${packet.baseData.size} bases!")
    }

    val handleAttackResult: (Connection, AttackResult, String) -> Unit = { _, packet, _ ->
        val currentProfile: ProfileData = KollaGO.INSTANCE.networkManager.ownProfile

        val xpDelta: Long = packet.updatedProfile!!.xp - currentProfile.xp
        val coinsDelta: Long = packet.updatedProfile.coins - currentProfile.coins

        Gdx.app.postRunnable {
            (KollaGO.INSTANCE.screen as ARWorld).closeOverlay()

            val dialog: GDXButtonDialog = KollaGO.INSTANCE.dialogs.newDialog(GDXButtonDialog::class.java)
            dialog.setTitle("Attakkk")
            dialog.setMessage("Result: ${packet.success}\n\nXP: $xpDelta\n\nCoins: $coinsDelta")
            dialog.addButton("Knykk")
            dialog.setCancelable(true)

            dialog.build().show()
        }

        KollaGO.INSTANCE.networkManager.applyProfileUpdate(packet.updatedProfile)
    }

    val handleProfileResponse: (Connection, ProfileResponse, String) -> Unit = {_, packet, _ ->
        KollaGO.INSTANCE.networkManager.applyProfileUpdate(packet.profile!!)
    }
}