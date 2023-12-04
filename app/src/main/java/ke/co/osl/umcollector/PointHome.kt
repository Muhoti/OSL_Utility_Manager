package ke.co.osl.umcollector

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import ke.co.osl.umcollector.Forms.*
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PointHome : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var webView: WebView
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var accuracy: TextView
    lateinit var tally: TextView
    lateinit var coords: TextView
    lateinit var back: ImageView
    var lat: Double = 0.0
    var lng: Double = 0.0
    var acc: Float = 0f
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    val ip_URL = "https://api-utilitymanager.mawasco.co.ke/api/homepage/"
//    var ip_URL = "http://192.168.1.136:3003/api/homepage/"


    object AndroidJSInterface {
        @JavascriptInterface
        fun onClicked() {
            Log.d("HelpButton", "Help button clicked")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_home)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
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

        //This section comprises of the navigation tools
        toolbar = findViewById(R.id.appbar)
        setSupportActionBar(toolbar)

        val mappedItem=intent.getStringExtra("MappedItem")
        accuracy = findViewById(R.id.accuracy)
        coords = findViewById(R.id.coords)
        tally = findViewById(R.id.tally)
        back = findViewById(R.id.back)

        showTotal()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestLocationPermission()
        }

        //Icons displayed on the Home Page
        val startMapping = findViewById<ImageView>(R.id.map)
        val myLocation = findViewById<ImageView>(R.id.location)
        val refresh = findViewById<ImageView>(R.id.refresh)

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        getLocationUpdates()

        myLocation.setOnClickListener {
            if (lat !== 0.0 && lng !== 0.0) {
                val txt = "Accuracy: " + acc.toString() + " m"
                accuracy.text = txt
                val txt1 = "Lat: " + lat.toString() + " Lng: " + lng.toString()
                coords.text = txt1
                adjustMarker(lng, lat)
                getLocationUpdates()
                showTotal()
            } else {
                getLocationUpdates()
                Toast.makeText(this, "Location not acquired! Please wait", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val progDailog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        progDailog.setCancelable(false);
        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        webView.addJavascriptInterface(AndroidJSInterface, "Android")
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                showSSLErrorDialog(handler)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progDailog.show()
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                progDailog.dismiss()
                if (ActivityCompat.checkSelfPermission(
                        this@PointHome,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@PointHome, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                }
                try {

                } catch (ex: IllegalStateException) {

                }
            }
        }
        webView.loadUrl(ip_URL)

        startMapping.setOnClickListener{
            if(lat !== 0.0 && lng !== 0.0){
                when(mappedItem){
                    "Point Projects" -> mapProjects()
                    "Customer Meters" -> mapCustomerMeters()
                    "Water Tanks" -> mapTanks()
                    "Valves" -> mapValves()
                    "Man Holes" -> mapManHoles()
                    "Master Meters" -> mapMasterMeters()
                    "PRV" -> mapPRV()
                    else -> {}
                }
                System.out.println("SYSTEM CHECKS: PAGE NUMBER ONE CONFIRMED!")
            }else {
                getLocationUpdates()
                Toast.makeText(this, "Location not acquired! Please wait", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        //Refresh Map
        refresh.setOnClickListener {
            refreshMap()
            getLocationUpdates()
            showTotal()
        }

        //Back to HomePage
        back.setOnClickListener {
            startActivity(Intent(this@PointHome, Home::class.java))
        }
    }

    private fun mapManHoles() {
        val intent = Intent(this, ManHolesForm::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapValves() {
        val intent = Intent(this, ValvesForm::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapTanks() {
        val intent = Intent(this, Tanks::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapMasterMeters() {
        val intent = Intent(this, MasterMeters::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapPRV() {
        val intent = Intent(this, PRV::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapProjects() {
        val intent = Intent(this, PointProjects::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun mapCustomerMeters() {
        val intent = Intent(this, Form1::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun showSSLErrorDialog(handler: SslErrorHandler?) {
        handler?.proceed()
    }

    //Display totals
    private fun showTotal() {

        val apiInterface = ApiInterface.create().showTotalMapped()

        apiInterface.enqueue(object :
            Callback<Message> {
            override fun onResponse(
                call: Call<Message>?,
                response: Response<Message>?
            ) {
                if (response?.body()?.success !== null) {
                    tally.text = "Total Mapped: " + response?.body()?.success
                }
            }

            override fun onFailure(
                call: Call<ke.co.osl.umcollector.models.Message>,
                t: Throwable
            ) {
                tally.text = "Total is not being displayed!"
            }
        })
    }

    private fun refreshMap() {
        webView.loadUrl(ip_URL)
    }

    fun adjustMarker(x: Double, y: Double) {
        webView.loadUrl(
            "javascript:(adjustMarker('$x','$y'))"
        )
    }

    fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 50
        locationRequest.fastestInterval = 50
        locationRequest.smallestDisplacement = 0.01f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function

//        try {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    System.out.println(locationResult.lastLocation)
                    val lc = locationResult.lastLocation
                    val txt = "Accuracy: " + lc?.accuracy.toString() + " m"
                    accuracy.text = txt
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
//        } catch (ex: IllegalStateException) {
//        }
    }

    //start location updates
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requestLocationPermission()
            } else Toast.makeText(this, "Location not acquired", Toast.LENGTH_LONG).show()
        } else mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // stop location updates
    fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //request location permission
    fun requestLocationPermission() {
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

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        webView.loadUrl(ip_URL)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, Home::class.java)
        startActivity(i)
        finish()
    }
}

