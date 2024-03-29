package skacce.rs.kollago.network.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.Connection
import de.tomgrill.gdxdialogs.core.dialogs.GDXButtonDialog
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.ar.overlays.AttackResultOverlay
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

                    if(it.ownerProfile!!.baseId == KollaGO.INSTANCE.networkManager.ownProfile.baseId) {
                        KollaGO.INSTANCE.networkManager.ownBase = it

                        Gdx.app.log("GameplayHandler", "Updated own base!")
                    }
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
            (KollaGO.INSTANCE.screen as ARWorld).showOverlay(AttackResultOverlay(coinsDelta, xpDelta, packet.success))
        }

        KollaGO.INSTANCE.networkManager.applyProfileUpdate(packet.updatedProfile)
    }

    val handleProfileResponse: (Connection, ProfileResponse, String) -> Unit = {_, packet, _ ->
        KollaGO.INSTANCE.networkManager.applyProfileUpdate(packet.profile!!)
    }
}