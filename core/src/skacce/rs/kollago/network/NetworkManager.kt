package skacce.rs.kollago.network

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.esotericsoftware.kryonet.Client
import okhttp3.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.oscim.core.GeoPoint
import skacce.rs.kollago.KollaGO
import skacce.rs.kollago.Platform
import skacce.rs.kollago.ar.ARWorld
import skacce.rs.kollago.network.handlers.GameplayNetworkHandler
import skacce.rs.kollago.network.protocol.*
import skacce.rs.kollago.screens.LoadingScreen
import skacce.rs.kollago.utils.toCoordinates
import java.io.IOException

class NetworkManager {
    private val game: KollaGO = KollaGO.INSTANCE

    private val apiUrl: String = game.platform.getRemoteString("central_api_address", "http://central.kollago.skacce.rs:8080")
    var gameServerAddress: String = ""
    var nominatimAddress: String = ""
    var firebaseUid: String = ""

    lateinit var kryoClient: Client
        private set

    lateinit var packetHandler: PacketHandler
        private set

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    var ownProfile: ProfileData = ProfileData()
        private set

    var ownBase: BaseData = BaseData()

    private val flagCache: MutableMap<String, Texture> = hashMapOf()
    private val stopTimeouts: MutableMap<String, Long> = hashMapOf()

    fun performPost(url: String, data: String, callback: (success: Boolean, body: String) -> Unit) {
        val request: Request = Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"), data)).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()

                callback(false, "")
            }

            override fun onResponse(call: Call, response: Response) {
                callback(true, response.body()?.string() ?: "")
            }
        })
    }

    fun beginLoginSequence() {
        game.screen = LoadingScreen(AuthenticationLoadingPerformer()) {
            game.screen = LoadingScreen(JoinGameLoadingPerformer()) {
                game.screen = ARWorld()
            }
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
                var message: String = response0["message"]?.toString() ?: response

                if(message.isBlank()) {
                    message = response
                }

                callback(errorCode, message)
            }
        }
    }

    private fun downloadTextureData(url: String, callback: (Boolean, Texture?) -> Unit) {
        Gdx.app.log("NetworkManager", "Downloading texture data from $url")
        val request: Request = Request.Builder().url(url).get().build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()

                callback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody: ResponseBody = response.body()!!

                val imageData: ByteArray = responseBody.bytes()

                Gdx.app.postRunnable {
                    val pixmap: Pixmap = Pixmap(imageData, 0, imageData.size)
                    val loadedTexture: Texture = Texture(pixmap)

                    callback(true, loadedTexture)
                }

                responseBody.close()
            }
        })
    }

    fun downloadFlag(flagId: String, callback: (Boolean, Texture?) -> Unit) {
        val flagTrace: Platform.NativePerformanceTrace = game.platform.createTrace("network_flag_download")
        flagTrace.start()

        if(flagCache.containsKey(flagId)) {
            callback(true, flagCache[flagId])

            flagTrace.incrementMetric("cache", 1)
            flagTrace.stop()
            return
        }

        flagTrace.incrementMetric("download", 1)

        downloadTextureData("$apiUrl/flag/$flagId?uid=$firebaseUid") { success, texture ->
            if(success) {
                flagCache[flagId] = texture!!
            }

            callback(success, texture)

            flagTrace.stop()
        }
    }

    fun joinGameServer(callback: (success: Boolean) -> Unit) {
        if(gameServerAddress.isBlank() || nominatimAddress.isBlank()) {
            callback(false)
            return
        }

        val joinTrace: Platform.NativePerformanceTrace = game.platform.createTrace("network_join_game")
        joinTrace.start()

        if(::kryoClient.isInitialized && kryoClient.isConnected) {
            kryoClient.dispose()
        }

        kryoClient = Client(100 * 1024, 100 * 1024)
        packetHandler = PacketHandler(kryoClient.kryo)

        // common.proto
        packetHandler.registerPacket(Coordinates::class)
        packetHandler.registerPacket(ProfileData::class)
        packetHandler.registerPacket(StopData::class)
        packetHandler.registerPacket(BaseData::class)

        // authentication.proto
        packetHandler.registerPacket(LoginRequest::class)
        packetHandler.registerPacket(LoginResponse::class)
        packetHandler.registerPacket(ProfileRequest::class)
        packetHandler.registerHandler(GameplayNetworkHandler.handleProfileResponse)
        packetHandler.registerPacket(UpdateProfile::class)

        // gameplay.proto

        packetHandler.registerPacket(NearStops::class)
        packetHandler.registerPacket(NearBases::class)
        packetHandler.registerHandler(GameplayNetworkHandler.handleFeatureResponse)
        packetHandler.registerPacket(CollectStop::class)
        packetHandler.registerPacket(CollectStopResponse::class)
        packetHandler.registerPacket(FetchBase::class)
        packetHandler.registerPacket(AttackBase::class)
        packetHandler.registerHandler(GameplayNetworkHandler.handleAttackResult)

        kryoClient.addListener(packetHandler)
        kryoClient.start()

        try {
            kryoClient.connect(5000, gameServerAddress, 32256)
        } catch (e: Exception) {
            e.printStackTrace()

            callback(false)

            joinTrace.stop()
            return
        }

        packetHandler.sendPacketForResponse(LoginRequest(firebaseUid), kryoClient) {
            val response: LoginResponse = it as LoginResponse

            if(response.errorCode != LoginResponse.ErrorCode.OK) {
                kryoClient.close()
                callback(false)

                return@sendPacketForResponse
            }

            applyProfileUpdate(response.ownProfile!!)

            callback(ownProfile.username.isNotBlank())
        }

        joinTrace.stop()
    }

    fun actualiseFeatures(location: Coordinates) {
        packetHandler.sendPacket(NearStops(firebaseUid, location), "", kryoClient)
        packetHandler.sendPacket(NearBases(firebaseUid, location), "", kryoClient)
    }

    fun updateBase(baseId: String, callback: (BaseData) -> Unit = {}) {
        packetHandler.sendPacketForResponse(FetchBase(firebaseUid, baseId), kryoClient) {
            callback((it as NearFeaturesResponse).baseData[0])
        }
    }

    fun updateProfile(callback: (success: Boolean) -> Unit) {
        if(!validateConnection()) {
            callback(false)
            return
        }

        packetHandler.sendPacketForResponse(ProfileRequest(firebaseUid), kryoClient) {
            applyProfileUpdate((it as ProfileResponse).profile!!)
            callback(ownProfile.username.isNotBlank())
        }
    }

    fun applyProfileUpdate(profile: ProfileData) {
        ownProfile = profile
        game.platform.updateCrashUser(firebaseUid, ownProfile)
    }

    fun actualiseTimeout(stop: StopData) {
        stopTimeouts[stop.stopId] = Math.max(stopTimeouts[stop.stopId] ?: 0, stop.timeout)
    }

    fun getStopTimeout(stop: StopData): Long = stopTimeouts[stop.stopId] ?: 0

    fun collectStop(stop: StopData, position: GeoPoint, callback: (profile: ProfileData) -> Unit) {
        if(getStopTimeout(stop) - System.currentTimeMillis() > 0) {
            return
        }

        stopTimeouts[stop.stopId] = System.currentTimeMillis() + 300000

        packetHandler.sendPacketForResponse(CollectStop(firebaseUid, position.toCoordinates(), stop.stopId), kryoClient) {
            callback((it as CollectStopResponse).updatedProfile!!)
        }
    }

    private fun validateConnection() = ::kryoClient.isInitialized && kryoClient.isConnected && firebaseUid.isNotBlank()
}