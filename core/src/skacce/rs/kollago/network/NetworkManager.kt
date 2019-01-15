package skacce.rs.kollago.network

import okhttp3.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.network.protocol.ProfileData
import skacce.rs.kollago.screens.LoadingScreen
import java.io.IOException

class NetworkManager {
    private val apiUrl: String = "http://192.168.1.108:8080"
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    fun performPost(url: String, data: String, callback: (success: Boolean, body: String) -> Unit) {
        val request: Request = Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"), data)).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "")
            }

            override fun onResponse(call: Call, response: Response) {
                callback(true, response.body()?.string() ?: "")
            }
        })
    }

    fun beginLoginSequence() {
        KollaGO.INSTANCE.screen = LoadingScreen(AuthenticationLoadingPerformer()) {
            KollaGO.INSTANCE.screen = ARWorld()
        }
    }

    fun tryApiLogin(uid: String, position: GeoPoint, callback: (errorCode: Int, message: String) -> Unit) {
        val request: JSONObject = JSONObject()
        request["firebase_uid"] = uid
        request["current_lat"] = position.latitude
        request["current_lon"] = position.longitude

        performApiRequest("login", request, callback)
    }

    fun tryApiRegister(uid: String, name: String, model: ProfileData.PlayerModel, basePosition: GeoPoint, callback: (errorCode: Int, message: String) -> Unit) {
        val request: JSONObject = JSONObject()
        request["firebase_uid"] = uid
        request["name"] = name
        request["model"] = model.value
        request["base_lat"] = basePosition.latitude
        request["base_lon"] = basePosition.longitude

        performApiRequest("register", request, callback)
    }

    private fun performApiRequest(path: String, data: JSONObject, callback: (Int, String) -> Unit) {
        performPost("$apiUrl/$path", data.toJSONString()) { success, response ->
            if(!success) {
                callback(-1, "")
            } else {
                val response0: JSONObject = JSONParser().parse(response) as JSONObject
                val errorCode: Int = response0["error_code"].toString().toInt()
                val message: String = response0["message"]?.toString() ?: ""

                callback(errorCode, message)
            }
        }
    }
}