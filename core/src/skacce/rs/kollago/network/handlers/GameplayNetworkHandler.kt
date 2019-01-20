package skacce.rs.kollago.network.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.esotericsoftware.kryonet.Connection
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.network.protocol.NearFeaturesResponse

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
}