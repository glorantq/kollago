package skacce.rs.kollago

import org.oscim.core.GeoPoint
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

    fun tracedLog(tag: String, message: String)

    fun debugString(): String

    interface NativeAuthUser {
        fun getEMail(): String
        fun getDisplayName(): String
        fun isEmailVerified(): Boolean

        fun refresh(callback: (success: Boolean, message: String) -> Unit)
        fun sendVerificationEmail()
    }
}