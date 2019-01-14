package skacce.rs.kollago.network

import okhttp3.*
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.screens.LoadingScreen
import java.io.IOException

class NetworkManager {
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
}