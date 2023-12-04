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
import android.view.View
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
import ke.co.osl.umcollector.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LineHome : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var webView: WebView
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var accuracy: TextView
    lateinit var coords: TextView
    lateinit var complete: ImageView
    lateinit var back: ImageView
    lateinit var error: TextView
    lateinit var mappedItem: String
    var lat: Double = 0.0
    var lng: Double = 0.0
    var acc: Float = 0f
    var ID: String = ""
    lateinit var Coordinates:ArrayList<ArrayList<Double>>
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

     val ip_URL = "https://api-utilitymanager.mawasco.co.ke/api/mapline/"
//    var ip_URL = "http://192.168.1.136:3003/api/mapline/"


    object AndroidJSInterface {
        @JavascriptInterface
        fun onClicked() {
            Log.d("HelpButton", "Help button clicked")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_home)

        mappedItem = intent.getStringExtra("MappedItem")!!
        System.out.println(mappedItem)

        //This section comprises of the navigation tools
        toolbar = findViewById(R.id.appbar)
        setSupportActionBar(toolbar)

        val addpoint = findViewById<Button>(R.id.addpoint)

        addpoint.setOnClickListener {
            System.out.println("mapped item is $mappedItem")
            when(mappedItem){
                "sewerlines" -> postSewerLineCoordinates(lng,lat)
                "waterpipes" -> postWaterPipeCoordinates(lng,lat)
                "lineprojects" -> postProjectLineCoordinates(lng,lat)
            }
        }

        accuracy = findViewById(R.id.accuracy)
        coords = findViewById(R.id.coords)
        complete = findViewById(R.id.complete)
        error = findViewById(R.id.error)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestLocationPermission()
        }
        //Icons displayed on the Home Page
        val myLocation = findViewById<ImageView>(R.id.location)
        val refresh = findViewById<ImageView>(R.id.refresh)
        back = findViewById(R.id.back)

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocationUpdates()

        myLocation.setOnClickListener {
            if (lat !== 0.0 && lng !== 0.0) {
                val txt = "Accuracy: " + acc.toString() + " m"
                accuracy.text = txt
                val txt1 = "Lat: " + lat.toString() + " Lng: " + lng.toString()
                coords.text = txt1
                getLocationUpdates()
            } else {
                getLocationUpdates()
                Toast.makeText(this, "Location not acquired! Please wait", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val progDailog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        progDailog.setCancelable(false);
        webView = findViewById<WebView>(R.id.webview)
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
                loadData(mappedItem)
                progDailog.dismiss()
                if (ActivityCompat.checkSelfPermission(
                        this@LineHome,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@LineHome, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                }
                try {
                    mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        System.out.println(location)
                        if (location != null) {
                            val txt = "Accuracy: " + location?.accuracy.toString() + " m"
                            accuracy.text = txt
                            val txt1 =
                                "Lat: " + location?.latitude?.toString() + " Lng: " + location?.longitude?.toString()
                            coords.text = txt1
                            adjustMarker(location?.longitude!!, location?.latitude!!)
                            lat = location!!.latitude
                            lng = location!!.longitude
                            acc = location!!.accuracy
                        }
                    }

                } catch (ex: IllegalStateException) {

                }
            }
        }

        webView.loadUrl(ip_URL)

        //Refresh Map
        refresh.setOnClickListener {
            refreshMap()
            getLocationUpdates()
        }

        //Back to HomePage
        back.setOnClickListener {
            startActivity(Intent(this@LineHome, Home::class.java))
        }

        complete.setOnClickListener {
            if(ID != "") {
                when(mappedItem){
                    "sewerlines" -> mapSewerLines()
                    "waterpipes" -> mapWaterPipes()
                    "lineprojects" -> mapProjectLines()
                }
            } else {
                error.text = "Map a line feature before submiting data."
                return@setOnClickListener
            }
        }

    }

    private fun mapSewerLines() {
        val intent = Intent(this, SewerlinesForm::class.java)
        intent.putExtra("ID", ID)
        startActivity(intent)
    }

    private fun mapProjectLines() {
        val intent = Intent(this, LineProjects::class.java)
        intent.putExtra("ID", ID)
        startActivity(intent)
    }

    private fun mapWaterPipes() {
        val intent = Intent(this, WaterPipesForm::class.java)
        intent.putExtra("ID", ID)
        System.out.println("The id is $ID")
        startActivity(intent)
    }

    private fun showSSLErrorDialog(handler: SslErrorHandler?) {
        handler?.proceed()
    }

    private fun refreshMap() {
        webView.loadUrl(ip_URL)
    }

    private fun adjustMarker(x: Double, y: Double) {
        webView.loadUrl(
            "javascript:(adjustMarker($x,$y))"
        )
    }

    private fun fetchLineData(coords: ArrayList<ArrayList<Double>>){
        System.out.println(coords)
        webView.loadUrl(
            "javascript:(fetchLineData($coords))"
        )
    }

    private fun loadData(type: String){
        webView.loadUrl(
            "javascript:(loadData('$type'))"
        )
    }

    private fun postSewerLineCoordinates(x: Double, y: Double) {
        System.out.println("Mapped Item is : $mappedItem")

        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE

        if(ID === "") {
            val coords:ArrayList<ArrayList<Double>> = arrayListOf(arrayListOf(x,y))

            val sewerlinesbody = SewerLinesBody(
                "",
                coords,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
            )

            val apiInterface = ApiInterface.create().postSewerlineCoords(sewerlinesbody)
            System.out.println("sewerline body is $sewerlinesbody")

            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println("connection made ${response?.body()}")
                    if (response?.body()?.success !== null) {
                        ID = response?.body()?.token!!
                        System.out.println("The ID IS now is " + response.body())
                        Coordinates = response?.body()?.coordinates!!
                        System.out.println("The coordinates are $Coordinates")
                        fetchLineData(response?.body()?.coordinates!!)
                    } else {
                        System.out.println("sewerline else is ${error.text}")

                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.println("The sewer error is ${error.text}")
                    error.text = "Connection to server failed!"
                }
            })
        }else {
            Coordinates = (Coordinates + arrayListOf(arrayListOf(x,y))) as ArrayList<ArrayList<Double>>
            val linedatacoordsbody = LineDataCoordsBody(
                Coordinates
            )

            System.out.println("Sewer coordinates: $Coordinates and id is $ID, $linedatacoordsbody")

            val apiInterface = ApiInterface.create().putCoordinatesLines(ID,linedatacoordsbody)
            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if (response?.body()?.success !== null) {
                        Coordinates = response?.body()?.coordinates!!
                        fetchLineData(response?.body()?.coordinates!!)
                    } else {
                        System.out.println("The SEWER ERROR are ${error.text}")
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.println("The SEWER FAIL are ${error.text}")
                    error.text = "Connection to server failed!"
                }
            })
        }
    }

    private fun postProjectLineCoordinates(x: Double, y: Double) {

        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE

        if(ID === "") {
            val coords:ArrayList<ArrayList<Double>> = arrayListOf(arrayListOf(x,y))

            val projectslinesbody = ProjectsLinesBody(
                "",
                "",
                "",
                coords,
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )

            val apiInterface = ApiInterface.create().postProjectlineCoords(projectslinesbody)

            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    System.out.println("line project connection made ${response?.body()}")
                    progress.visibility = View.GONE
                    if (response?.body()?.success !== null) {
                        ID = response?.body()?.token!!
                        Coordinates = response?.body()?.coordinates!!
                        System.out.println("The coordinates are $Coordinates")
                        fetchLineData(response?.body()?.coordinates!!)
                    } else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }else {
            Coordinates = (Coordinates + arrayListOf(arrayListOf(x,y))) as ArrayList<ArrayList<Double>>
            val linedatacoordsbody = LineDataCoordsBody(
                Coordinates
            )

            System.out.println("Line project coordinates: $Coordinates and id is $ID, $linedatacoordsbody")

            val apiInterface = ApiInterface.create().putProjectsCoordinatesLines(ID,linedatacoordsbody)
            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if (response?.body()?.success !== null) {
                        Coordinates = response?.body()?.coordinates!!
                        fetchLineData(response?.body()?.coordinates!!)
                    } else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }
    }

    private fun postWaterPipeCoordinates(x: Double, y: Double) {
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE

        if(ID === "") {
            val coords:ArrayList<ArrayList<Double>> = arrayListOf(arrayListOf(x,y))
            val waterLinesBody = WaterLinesCoordsBody(
                "",
                coords,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )

            val apiInterface = ApiInterface.create().postWaterPipesCoords(waterLinesBody)
            System.out.println(waterLinesBody)

            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if (response?.body()?.success !== null) {
                        ID = response.body()?.token!!
                        System.out.println("The ID IS now is " + response.body())
                        Coordinates = response.body()?.coordinates!!
                        System.out.println("The coordinates are $Coordinates")
                        fetchLineData(response.body()?.coordinates!!)
                    } else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }else {
            Coordinates = (Coordinates + arrayListOf(arrayListOf(x,y))) as ArrayList<ArrayList<Double>>
            val linedatacoordsbody = LineDataCoordsBody(
                Coordinates
            )

            System.out.println("water project coordinates are: $Coordinates and id is $ID, $linedatacoordsbody")

            val apiInterface = ApiInterface.create().putCoordinatesWaterPipes(ID,linedatacoordsbody)
            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if (response?.body()?.success !== null) {
                        Coordinates = response?.body()?.coordinates!!
                        fetchLineData(response?.body()?.coordinates!!)
                    } else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.print("api onFailure is ${ error.text }")

                    error.text = "Connection to server failed!!"
                }
            })
        }
    }

    fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 50
        locationRequest.fastestInterval = 50
        locationRequest.smallestDisplacement = 0.01f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
      try {
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
                    lat = lc!!.latitude
                    lng = lc!!.longitude
                    acc = lc!!.accuracy
                    adjustMarker(lc!!.longitude, lc!!.latitude)
                }

            }
        }
        } catch (ex: IllegalStateException) {
          System.out.println(ex)
        }
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
        } else {
            locationRequest = LocationRequest()
            locationRequest.interval = 50
            locationRequest.fastestInterval = 50
            locationRequest.smallestDisplacement = 0.01f // 170 m = 0.1 mile
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
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
