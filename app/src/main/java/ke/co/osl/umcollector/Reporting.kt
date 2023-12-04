package ke.co.osl.umcollector

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.auth0.android.jwt.JWT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.Message
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.Permissions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


open class Reporting: AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var webview: WebView
    private lateinit var locationRequest: LocationRequest
    lateinit var accuracy: TextView
    lateinit var coords: TextView
    private val CAMERA = 1
    private val CAMERA_REQUEST_CODE = 123

    var lat: Double = 0.0
    var lng: Double = 0.0
    var acc: Float = 10f
    var requestingLocationUpdates : Boolean = false
    lateinit var imageView: ImageView
    private var mCurrentPhotoPath: String? = null
    private lateinit var locationCallback: LocationCallback
    val ip_URL = "https://api-utilitymanager.mawasco.co.ke/api/homepage"

    lateinit var myBitmap:Bitmap;
    lateinit var picUri: Uri

    lateinit var  permissionsToRequest:ArrayList<Permissions>
    var  permissionsRejected:ArrayList<Permissions> =  ArrayList();
    var  permissions:ArrayList<Permissions> =  ArrayList();
    val ALL_PERMISSIONS_RESULT = 107

    private lateinit var imageCapture: ImageCapture

    object AndroidJSInterface {
        @JavascriptInterface
        fun onClicked() {
            Log.d("HelpButton", "Help button clicked")
        }
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/Pictures"
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporting)

        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )

        initLocation()
        requestMultiplePermissions()

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        val jwt = JWT(preferences.getString("publictoken","")!!)
        val userid = jwt.getClaim("UserID").asString()

        val back = findViewById<ImageView>(R.id.back)
        accuracy = findViewById(R.id.accuracy)
        coords = findViewById(R.id.coords)
        imageView = findViewById(R.id.photo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestLocationPermission()
        }

        back.setOnClickListener {
            startActivity(Intent(this, Incidences::class.java))
        }

        initWebView()
        reportIncident ()
    }

    private fun initWebView(){
        val progDialog: ProgressDialog? = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDialog?.setCancelable(false);

        webview = findViewById(R.id.webview)
        webview.webViewClient = WebViewClient()

        val webSettings = webview.settings
        webSettings.javaScriptEnabled = true

        webview.addJavascriptInterface(AndroidJSInterface, "Android")
        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                if (handler != null){
                    handler.proceed();
                } else {
                    super.onReceivedSslError(view, null, error);
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progDialog?.show()
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                progDialog?.dismiss()
              //  getLocationUpdates()
                adjustMarker(lng, lat)
            }
        }
        webview.loadUrl(ip_URL)
    }
    private fun initLocation(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(10)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                System.out.println("location updates")
                for (location in locationResult.locations){
                    System.out.println(location)
                    val txt = "Accuracy: " + location?.accuracy.toString() + " m"
                    accuracy.text = txt
                    val txt1 =
                        "Lat: " + location?.latitude?.toString() + " Lng: " + location?.longitude?.toString()
                    coords.text = txt1
                    System.out.println(location)
                    adjustMarker(location?.longitude!!, location?.latitude!!)
                    lat = location!!.latitude
                    lng = location!!.longitude
                    acc = location!!.accuracy
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

        mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                requestingLocationUpdates = true
                val txt = "Accuracy: " + location?.accuracy.toString() + " m"
                accuracy.text = txt
                val txt1 =
                    "Lat: " + location?.latitude?.toString() + " Lng: " + location?.longitude?.toString()
                coords.text = txt1
                System.out.println(location)
                adjustMarker(location?.longitude!!, location?.latitude!!)
                lat = location!!.latitude
                lng = location!!.longitude
                acc = location!!.accuracy
                System.out.println("ohoo " + acc)
            }
        }
        getLocationUpdates()
    }
    fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 50
        locationRequest.fastestInterval = 50
        locationRequest.smallestDisplacement = 0.01f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //
        try {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    System.out.println("update called")
                    if (locationResult.locations.isNotEmpty()) {
                        System.out.println(locationResult.lastLocation)
                        val lc = locationResult.lastLocation
                        val txt = "Accuracy: " + lc?.accuracy.toString() + " m"
                        accuracy.text = txt
                        val accuracy2 = accuracy.toString()
                        System.out.println("NOW HERE " + accuracy2)
                        val txt1 =
                            "Lat: " + lc?.latitude.toString() + " Lng: " + lc?.longitude!!.toString()
                        coords.text = txt1
                        adjustMarker(lc?.longitude!!, lc?.latitude!!)
                        lat = lc!!.latitude
                        lng = lc!!.longitude
                        acc = lc!!.accuracy
                    }
                }
            }
        }catch (e:Exception){
        }
    }

    private fun adjustMarker(longitude: Double, latitude: Double) {
        webview.loadUrl(
            "javascript:(adjustMarker($longitude,$latitude))"
        )
    }

    private fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    }
                    else -> {
                    }
                }
            }
        }
        // 7o7gi9h9
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun reportIncident() {
        val reportedIncident=intent.getStringExtra("ReportIncident")
        val takePhoto = findViewById<ImageView>(R.id.takePhoto)
        val error = findViewById<TextView>(R.id.error)
        val submit = findViewById<Button>(R.id.submit)
        val comment = findViewById<TextView>(R.id.comment)
        val progress = findViewById<ProgressBar>(R.id.progress)

        takePhoto.isEnabled = false


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 111)
        } else {
            takePhoto.isEnabled = true
        }

        takePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }

        submit.setOnClickListener {
            val jwt = JWT(preferences.getString("publictoken","")!!)
            val userID = jwt.getClaim("UserID").asString()

            error.visibility = View.GONE

            val file = File(mCurrentPhotoPath)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

            if(acc > 50.00){
                error.visibility = View.VISIBLE
                error.text = "Accuracy must be below 50m. Please wait!!"
                val countdonwtimer = object: CountDownTimer(1000,1){
                    override fun onTick(p0: Long) {
                    }
                    override fun onFinish() {
                        error.visibility = View.GONE
                    }
                }
                countdonwtimer.start()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(comment.text.toString())) {
                error.visibility = View.VISIBLE
                error.text = "Please describe the incident!"
                val countdonwtimer = object: CountDownTimer(1000,1){
                    override fun onTick(p0: Long) {
                    }
                    override fun onFinish() {
                        error.visibility = View.GONE
                    }
                }
                countdonwtimer.start()
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Latitude", lat.toString())
                .addFormDataPart("Type", reportedIncident.toString())
                .addFormDataPart("Status", "Received")
                .addFormDataPart("NRWUserID", "")
                .addFormDataPart("DueDate", "")
                .addFormDataPart("Action", "")
                .addFormDataPart("Longitude", lng.toString())
                .addFormDataPart("Description", comment.text.toString())
                .addFormDataPart("Image", file.name, requestFile)
                .addFormDataPart("UserID", userID)
                .build()

            val apiInterface = ApiInterface.create().reportIncident(multipartBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        error.visibility = View.VISIBLE
                        error.text = response?.body()?.success

                        val countdonwtimer = object: CountDownTimer(2000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                error.visibility = View.GONE
                                startActivity(Intent(this@Reporting, Incidences::class.java))
                                finish()
                            }
                        }
                        countdonwtimer.start()
                    }
                    else {
                        error.visibility = View.VISIBLE
                        error.text = response?.body()?.error
                        val countdonwtimer = object: CountDownTimer(1000,1){
                            override fun onTick(p0: Long) {
                            }
                            override fun onFinish() {
                                error.visibility = View.GONE
                            }
                        }
                        countdonwtimer.start()
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                    val countdonwtimer = object: CountDownTimer(1000,1){
                        override fun onTick(p0: Long) {
                        }
                        override fun onFinish() {
                            error.visibility = View.GONE
                        }
                    }
                    countdonwtimer.start()
                }
            })
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo)

            mCurrentPhotoPath = saveImageToFile(photo)
        }
    }

    private fun saveImageToFile(bitmap: Bitmap): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.jpg"
        val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, fileName)

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    private fun startLocationUpdates() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission();
            return
        }
       if(isGpsEnabled){
            mFusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }
    private fun requestMultiplePermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false) -> {
                    }
                    permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) -> {
                    }
                    else -> {
                    }
                }
            }
        }
        // 7o7gi9h9
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
    override fun onResume() {
        super.onResume()
//        if (requestingLocationUpdates) startLocationUpdates()
    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

}