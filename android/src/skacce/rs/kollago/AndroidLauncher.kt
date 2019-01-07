package skacce.rs.kollago

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18
import com.badlogic.gdx.utils.SharedLibraryLoader
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.perf.FirebasePerformance
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
    private val REQUEST_GOOGLE_SIGN_IN = 41672

    private lateinit var lastPosition: GeoPoint
    private lateinit var locationManager: LocationManager
    private lateinit var locationInitFuture: CompletableFuture<Boolean>

    private var statusBarHeight: Int = 0

    private var androidHeight: Float = 0f
    private lateinit var inputLayout: LinearLayout
    private lateinit var textInput: EditText
    private val textInputListeners = CopyOnWriteArrayList<TextInputListener>()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    private val googleRequestMap: MutableMap<Int, (Boolean) -> Unit> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true

        firebaseAuth = FirebaseAuth.getInstance()
        val signInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_sign_in_client))
                .requestEmail()
                .build()

        googleClient = GoogleSignIn.getClient(this, signInOptions)

        AndroidGraphics.init()
        GdxAssets.init("")
        GLAdapter.init(GdxGL())

        Tile.SIZE = Tile.calculateTileSize()

        val window = window
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
        inputLayout.y = 0f

        val relativeLayout = RelativeLayout(this)
        val gameViewParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        gameView.layoutParams = gameViewParams

        relativeLayout.addView(gameView)

        val inputParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        inputLayout.layoutParams = inputParams
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

        if(requestCode > REQUEST_GOOGLE_SIGN_IN) {
            val offset: Int = requestCode - REQUEST_GOOGLE_SIGN_IN
            val callback: (Boolean) -> Unit = googleRequestMap[offset]!!
            googleRequestMap.remove(offset)

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                handleGoogleLogin(account, callback)
            } catch (e: ApiException) {
                Log.e("Authentication", "Failed to sign in with Google!")
                callback(false)
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

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            lastPosition = GeoPoint(p0.latitude, p0.longitude)
        }

        override fun onStatusChanged(p0: String, p1: Int, p2: Bundle) {

        }

        override fun onProviderEnabled(p0: String) {

        }

        override fun onProviderDisabled(p0: String) {

        }
    }

    private fun setUpLocationManager() {
        Log.i("Location", "Setting up LocationManager")

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

            if (!::lastPosition.isInitialized) {
                var location: Location? = null

                locationManager.allProviders.forEach {
                    val lastProviderLocation: Location? = locationManager.getLastKnownLocation(it)

                    if (lastProviderLocation != null && (location == null || location!!.accuracy < lastProviderLocation.accuracy)) {
                        location = lastProviderLocation
                    }
                }

                if (location != null) {
                    lastPosition = GeoPoint(location!!.latitude, location!!.longitude)
                }
            }

            locationInitFuture.complete(true)
        } else {
            locationInitFuture.complete(false)
        }

        Log.i("Location", "Set up LocationManager!")
    }

    override fun checkGpsState(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ::locationManager.isInitialized &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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

            textInput.inputType = if (type === TextInputProvider.InputType.NUMBER) android.text.InputType.TYPE_CLASS_NUMBER else android.text.InputType.TYPE_CLASS_TEXT

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
            callback(it.result!!.token!!)
        }?.addOnFailureListener {
            Log.e("Authentication", "Failed to get UID!")
            it.printStackTrace()

            callback("")
        }?.addOnCanceledListener {
            Log.e("Authentication", "getIdToken Task got canceled!")

            callback("")
        } ?: callback("")
    }

    override fun performGoogleLogIn(callback: (success: Boolean) -> Unit) {
        val signInIntent: Intent = googleClient.signInIntent

        val requestOffset: Int = Random().nextInt()
        googleRequestMap[requestOffset] = callback

        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN + requestOffset)
    }

    override fun performEmailAuth(email: String, password: String, register: Boolean, callback: (success: Boolean) -> Unit) {
        if (register) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                Log.i("Authentication", "User successfully registered!")

                callback(true)
            }.addOnFailureListener {
                Log.e("Authentication", "Failed to register account!")
                it.printStackTrace()

                callback(false)
            }.addOnCanceledListener {
                Log.e("Authentication", "createUserWithEmailAndPassword Task got canceled!")

                callback(false)
            }
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                Log.i("Authentication", "User successfully signed in!")

                callback(true)
            }.addOnFailureListener {
                Log.e("Authentication", "Failed to log in!")
                it.printStackTrace()

                callback(false)
            }.addOnCanceledListener {
                callback(false)
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

    private fun handleGoogleLogin(user: GoogleSignInAccount, callback: (success: Boolean) -> Unit) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(user.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {
            Log.i("Authentication", "User successfully signed in!")

            callback(true)
        } .addOnFailureListener {
            Log.e("Authentication", "Failed to log in!")
            it.printStackTrace()

            callback(false)
        }.addOnCanceledListener {
            callback(false)
        }
    }

    override fun getUser(): Platform.NativeAuthUser {
        return FirebaseAuthUser(firebaseAuth.currentUser!!)
    }

    class FirebaseAuthUser(private val firebaseUser: FirebaseUser) : Platform.NativeAuthUser {
        override fun getEMail(): String {
            return firebaseUser.email!!
        }

        override fun getDisplayName(): String {
            return firebaseUser.displayName!!
        }
    }
}
