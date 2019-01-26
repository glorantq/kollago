package skacce.rs.kollago

import org.oscim.core.GeoPoint
import skacce.rs.kollago.network.protocol.ProfileData
import java.util.concurrent.CompletableFuture

interface Platform {
    fun initGps(): CompletableFuture<Boolean>
    fun checkGpsState(): Boolean
    fun getGpsPosition(): GeoPoint

    fun getSafeAreaOffset(): Int

    fun isLoggedIn(): Boolean
    fun getUser(): NativeAuthUser
    fun getFirebaseUID(callback: (token: String) -> Unit)
    fun isEMailVerified(uid: String): Boolean

    fun performGoogleLogIn(callback: (success: Boolean, message: String) -> Unit)
    fun performEmailAuth(email: String, password: String, register: Boolean, callback: (success: Boolean, message: String) -> Unit)

    fun logOut(callback: () -> Unit)

    fun backupGeocode(position: GeoPoint): String

    fun updateRemoteConfig(callback: (success: Boolean) -> Unit)
    fun getRemoteString(key: String, default: String): String

    fun updateCrashUser(uid: String, profile: ProfileData)

    fun createTrace(traceName: String): NativePerformanceTrace

    interface NativeAuthUser {
        fun getEMail(): String
        fun getDisplayName(): String
        fun isEmailVerified(): Boolean

        fun refresh(callback: (success: Boolean, message: String) -> Unit)
        fun sendVerificationEmail()
    }

    interface NativePerformanceTrace {
        fun getAttribute(attribute: String): String?
        fun getAttributes(): Map<String, String>
        fun getLongMetric(metricName: String): Long
        fun incrementMetric(metricName: String, incrementBy: Long)
        fun putAttribute(attribute: String, value: String)
        fun putMetric(metricName: String, value: Long)
        fun removeAttribute(attribute: String)
        fun start()
        fun stop()

        operator fun get(attribute: String): String? = getAttribute(attribute)
        operator fun set(attribute: String, value: String) = putAttribute(attribute, value)
    }
}