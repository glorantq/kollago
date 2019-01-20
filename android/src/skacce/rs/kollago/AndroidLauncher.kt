package skacce.rs.kollago

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.*
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18
import com.badlogic.gdx.utils.SharedLibraryLoader
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.backend.GLAdapter
import org.oscim.core.GeoPoint
import org.oscim.core.Tile
import org.oscim.gdx.GdxAssets
import skacce.rs.kollago.input.text.TextInputListener
import skacce.rs.kollago.input.text.TextInputProvider
import skacce.rs.kollago.map.GdxGL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

class AndroidLauncher : AndroidApplication(), Platform, TextInputProvider {
    private val REQUEST_LOCATION_PERMISSION: Int = 5322
    private val REQUEST_CHECK_SETTINGS: Int = 1423
    private val REQUEST_GOOGLE_SIGN_IN = 41672

    private lateinit var lastPosition: GeoPoint
    private lateinit var lastLocation: Location

    private lateinit var locationManager: FusedLocationProviderClient
    private lateinit var locationInitFuture: CompletableFuture<Boolean>

    private var statusBarHeight: Int = 0

    private var androidHeight: Float = 0f
    private lateinit var inputLayout: LinearLayout
    private lateinit var textInput: EditText
    private val textInputListeners = CopyOnWriteArrayList<TextInputListener>()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private val googleRequestMap: MutableMap<Int, (Boolean, String) -> Unit> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true

        firebaseAuth = FirebaseAuth.getInstance()
        val signInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_sign_in_client))
                .requestEmail()
                .build()

        googleClient = GoogleSignIn.getClient(this, signInOptions)

        remoteConfig = FirebaseRemoteConfig.getInstance()

        AndroidGraphics.init()
        GdxAssets.init("")
        GLAdapter.init(GdxGL())

        Tile.SIZE = Tile.calculateTileSize()

        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val size = Rect()
            val view = window.decorView

            view.getWindowVisibleDisplayFrame(size)

            androidHeight = size.height().toFloat()

            runOnUiThread {
                val newParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                newParams.topMargin = androidHeight.toInt() - inputLayout.height
                inputLayout.layoutParams = newParams
            }
        }

        val config: AndroidApplicationConfiguration = AndroidApplicationConfiguration()
        config.useImmersiveMode = false
        config.useAccelerometer = true
        config.useGyroscope = false
        config.useCompass = true
        config.stencil = 8
        config.numSamples = 2
        config.useWakelock = true
        config.hideStatusBar = false

        SharedLibraryLoader().load("vtm-jni")

        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")

        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }

        val gameView: View = initializeForView(KollaGO.create(this, this), config)

        if (gameView is GLSurfaceView) {
            gameView.preserveEGLContextOnPause = true
        } else {
            (gameView as GLSurfaceView20API18).preserveEGLContextOnPause = true
        }

        inputLayout = LinearLayout(this)

        textInput = EditText(this)
        val editTextParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        textInput.layoutParams = editTextParams
        textInput.setSingleLine(true)

        val okButton = Button(this)
        okButton.text = "OK"
        val buttonParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        okButton.layoutParams = buttonParams

        inputLayout.addView(textInput)
        inputLayout.addView(okButton)
        inputLayout.y = statusBarHeight.toFloat()

        val relativeLayout = RelativeLayout(this)
        val gameViewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        gameView.layoutParams = gameViewParams

        relativeLayout.addView(gameView)

        val inputParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        inputLayout.layoutParams = inputParams
        inputLayout.setBackgroundColor(Color.WHITE)
        relativeLayout.addView(inputLayout)

        setContentView(relativeLayout)

        inputLayout.visibility = View.INVISIBLE

        okButton.setOnClickListener {
            closeTextInput()
            broadcastTextChange()
        }

        textInput.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                closeTextInput()
                return@setOnKeyListener true
            }

            false
        }

        textInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                broadcastTextChange()
            }
        })

        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        Log.i("StatusBar", "Height: $statusBarHeight")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i("Result", "Code: $requestCode")

        if (requestCode > REQUEST_GOOGLE_SIGN_IN) {
            val offset: Int = requestCode - REQUEST_GOOGLE_SIGN_IN
            val callback: (Boolean, String) -> Unit = googleRequestMap[offset]!!
            googleRequestMap.remove(offset)

            Log.i("Authentication", "Handling Google callback, offset: $offset")

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                handleGoogleLogin(account, callback)
            } catch (e: ApiException) {
                Log.e("Authentication", "Failed to sign in with Google!")
                callback(false, e.localizedMessage)
            }
        }

        if(requestCode == REQUEST_CHECK_SETTINGS) {
            if(resultCode == Activity.RESULT_OK) {
                setUpGPS()
            } else {
                locationInitFuture.complete(false)
            }
        }
    }

    override fun initGps(): CompletableFuture<Boolean> {
        if (::locationInitFuture.isInitialized && !locationInitFuture.isDone) {
            throw RuntimeException("GPS is already being initialised!")
        }

        val future: CompletableFuture<Boolean> = CompletableFuture()
        locationInitFuture = future

        runOnUiThread {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("Location", "Requesting permission...")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            } else {
                setUpLocationManager()
            }
        }

        return future
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("Location", "Got permission response!")

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpLocationManager()
                } else {
                    locationInitFuture.complete(false)
                }
            }
        }
    }

    private val locationListener: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            val firstFix: Boolean = !::lastPosition.isInitialized

            lastLocation = p0!!.lastLocation
            lastPosition = GeoPoint(lastLocation.latitude, lastLocation.longitude)

            if(firstFix) {
                locationInitFuture.complete(true)
            }
        }
    }

    private fun setUpLocationManager() {
        tracedLog("Location", "Setting up LocationManager")

        locationManager = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val requestBuilder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)

        tracedLog("Location", "Checking location settings")
        settingsClient.checkLocationSettings(requestBuilder.build()).addOnSuccessListener {
            setUpGPS()
        }.addOnFailureListener {
            if(it is ResolvableApiException) {
                try {
                    tracedLog("Location", "Starting resolution")
                    it.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (e: Exception) {
                    locationInitFuture.complete(false)
                }
            } else {
                locationInitFuture.complete(false)
            }
        }
    }

    private fun setUpGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            tracedLog("Location", "Setting up location client")

            val locationRequest: LocationRequest = LocationRequest.create().apply {
                interval = 50
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationManager.requestLocationUpdates(locationRequest, locationListener, null)
            locationManager.lastLocation.addOnSuccessListener {
                if(it != null) {
                    lastLocation = it
                    lastPosition = GeoPoint(it.latitude, it.longitude)

                    locationInitFuture.complete(true)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Nincs elérhető pozíció! A jaték elindul amint frissül a GPS.", Toast.LENGTH_LONG).show()
                    }
                }

                tracedLog("Location", "GMS location setup complete")
            }.addOnFailureListener {
                locationInitFuture.complete(false)
            }
        } else {
            locationInitFuture.complete(false)
        }
    }

    override fun checkGpsState(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ::locationManager.isInitialized
    }

    override fun getGpsPosition(): GeoPoint {
        return lastPosition
    }

    override fun getSafeAreaOffset(): Int {
        return statusBarHeight
    }

    override fun registerListener(textInputListener: TextInputListener) {
        textInputListeners.add(textInputListener)
    }

    override fun removeListener(textInputListener: TextInputListener) {
        textInputListeners.remove(textInputListener)
    }

    override fun openTextInput(placeholder: String, text: String, type: TextInputProvider.InputType, maxChars: Int) {
        runOnUiThread {
            textInput.setText(text)
            textInput.hint = placeholder

            textInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxChars))

            textInput.inputType = when {
                type === TextInputProvider.InputType.NUMBER -> android.text.InputType.TYPE_CLASS_NUMBER
                type === TextInputProvider.InputType.TEXT -> android.text.InputType.TYPE_CLASS_TEXT
                else -> InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            inputLayout.visibility = View.VISIBLE
            textInput.requestFocus()

            changeKeyboard(true)
        }
    }

    override fun closeTextInput() {
        runOnUiThread {
            inputLayout.visibility = View.INVISIBLE

            changeKeyboard(false)
        }

        broadcastTextChange()
    }

    private fun changeKeyboard(show: Boolean) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus

        if (view == null) {
            view = View(this@AndroidLauncher)
        }

        if (!show) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } else {
            imm.showSoftInput(textInput, 0)
        }
    }

    private fun broadcastTextChange() {
        textInputListeners.forEach { it -> it.textUpdated(textInput.getText().toString()) }
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getFirebaseUID(callback: (token: String) -> Unit) {
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            callback(it.result?.token ?: "")
        }?.addOnFailureListener {
            Log.e("Authentication", "Failed to get UID!")
            it.printStackTrace()

            callback("")
        }?.addOnCanceledListener {
            Log.e("Authentication", "getIdToken Task got canceled!")

            callback("")
        } ?: callback("")
    }

    override fun performGoogleLogIn(callback: (success: Boolean, message: String) -> Unit) {
        val signInIntent: Intent = googleClient.signInIntent

        val requestOffset: Int = Random().nextInt(600) + 1
        googleRequestMap[requestOffset] = callback

        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN + requestOffset)
    }

    override fun performEmailAuth(email: String, password: String, register: Boolean, callback: (success: Boolean, message: String) -> Unit) {
        if (register) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                Log.i("Authentication", "User successfully registered!")

                it.user.sendEmailVerification()
                callback(true, "")
            }.addOnFailureListener {
                Log.e("Authentication", "Failed to register account!")
                it.printStackTrace()

                callback(false, it.localizedMessage)
            }.addOnCanceledListener {
                Log.e("Authentication", "createUserWithEmailAndPassword Task got canceled!")

                callback(false, "Megszakítva")
            }
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                Log.i("Authentication", "User successfully signed in!")

                callback(true, "")
            }.addOnFailureListener {
                Log.e("Authentication", "Failed to log in!")
                it.printStackTrace()

                callback(false, it.localizedMessage)
            }.addOnCanceledListener {
                callback(false, "Megszakítva")
            }
        }
    }

    override fun logOut(callback: () -> Unit) {
        firebaseAuth.signOut()
        googleClient.signOut()

        callback()
    }

    override fun isEMailVerified(uid: String): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified ?: false
    }

    private fun handleGoogleLogin(user: GoogleSignInAccount, callback: (success: Boolean, message: String) -> Unit) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(user.idToken, null)
        Log.i("Authentication", "Google getCredentials")

        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            Log.i("Authentication", "User successfully signed in!")

            callback(true, "")
        }.addOnFailureListener {
            Log.e("Authentication", "Failed to log in!")
            it.printStackTrace()

            callback(false, it.localizedMessage)
        }.addOnCanceledListener {
            callback(false, "Megszakítva")
        }
    }

    override fun getUser(): Platform.NativeAuthUser {
        return FirebaseAuthUser(firebaseAuth.currentUser!!)
    }

    override fun backupGeocode(position: GeoPoint): String {
        val geocoder: Geocoder = Geocoder(this)

        val results: List<Address> = geocoder.getFromLocation(position.latitude, position.longitude, 1)
        if(results.isEmpty()) {
            return "Ismeretlen"
        }

        return with(results[0]) {
            (0..this.maxAddressLineIndex).map { getAddressLine(it).replace("\n", "") }
        }.joinToString(", ")
    }

    class FirebaseAuthUser(private val firebaseUser: FirebaseUser) : Platform.NativeAuthUser {
        override fun getEMail(): String {
            return firebaseUser.email!!
        }

        override fun getDisplayName(): String {
            return firebaseUser.displayName ?: firebaseUser.email ?: ""
        }

        override fun isEmailVerified(): Boolean {
            return firebaseUser.isEmailVerified
        }

        override fun refresh(callback: (success: Boolean, message: String) -> Unit) {
            firebaseUser.reload().addOnCompleteListener {
                callback(true, "")
            }.addOnFailureListener {
                callback(false, it.localizedMessage)
            }.addOnCanceledListener {
                callback(false, "Megszakítva")
            }
        }

        override fun sendVerificationEmail() {
            firebaseUser.sendEmailVerification()
        }
    }

    override fun updateRemoteConfig(callback: (success: Boolean) -> Unit) {
        remoteConfig.fetch().addOnCompleteListener {
            remoteConfig.activateFetched()
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }

    override fun getRemoteString(key: String, default: String): String {
        remoteConfig.setDefaults(mapOf(key to default))

        return remoteConfig.getString(key)
    }

    override fun tracedLog(tag: String, message: String) {
        Crashlytics.log(Log.INFO, tag, message)
    }

    override fun debugString(): String {
        return "Loc: $lastLocation, acc: ${lastLocation.accuracy}, prov.: ${lastLocation.provider}"
    }
}
